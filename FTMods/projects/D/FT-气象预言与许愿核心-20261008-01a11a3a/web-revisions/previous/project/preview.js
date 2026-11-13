const fs = require('fs');
const path = require('path');
const http = require('http');
const assert = require('assert/strict');

const root = __dirname;
const mime = {'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.json':'application/json','.png':'image/png'};

function createServer(){
    return http.createServer((request,response)=>{
        const requested = decodeURIComponent(new URL(request.url,'http://localhost').pathname);
        if(requested==='/favicon.ico'){response.writeHead(204);response.end();return;}
        const filename = path.resolve(root,requested==='/'?'viewer.html':'.'+requested);
        if(!filename.startsWith(root+path.sep)){response.writeHead(403);response.end();return;}
        if(!fs.existsSync(filename)||!fs.statSync(filename).isFile()){response.writeHead(404);response.end();return;}
        response.setHeader('Content-Type',mime[path.extname(filename)]||'application/octet-stream');
        fs.createReadStream(filename).pipe(response);
    });
}

async function verify(server){
    const {chromium} = require('playwright');
    const browser = await chromium.launch({channel:'msedge',headless:true,args:['--enable-webgl','--use-angle=swiftshader','--enable-unsafe-swiftshader']});
    const page = await browser.newPage({viewport:{width:1440,height:1040},deviceScaleFactor:1});
    const errors = [];
    page.on('pageerror',error=>errors.push(error.message));
    page.on('console',message=>{if(message.type()==='error')errors.push(message.text());});
    const report = {models:[],errors,previewOnly:true,gameLightingNotSimulated:true};
    const output = path.join(root,'previews');
    fs.mkdirSync(output,{recursive:true});
    try{
        await page.goto('http://127.0.0.1:'+server.address().port);
        await page.waitForFunction(()=>window.ftPreviewReady);
        await page.waitForTimeout(150);
        await page.screenshot({path:path.join(output,'overview.png')});
        for(const name of ['weather_forecaster','oracle','wishing_well_core']){
            await page.click(`[data-model="${name}"]`);
            const views = [];
            for(const view of ['perspective','front','back','left','right','top','bottom']){
                await page.click(`[data-view="${view}"]`);
                await page.waitForTimeout(80);
                const state = await page.evaluate(()=>ftPreview.snapshot());
                assert.equal(state.active,name);
                assert.equal(state.view,view);
                assert(state.triangles>0);
                await page.locator('canvas').screenshot({path:path.join(output,name+'_'+view+'.png')});
                views.push(view);
            }
            report.models.push({name,views,elementCount:(await page.evaluate(()=>ftPreview.snapshot())).modelCounts[name]});
        }
        await page.click('[data-model="wishing_well_core"]');
        await page.click('[data-view="perspective"]');
        await page.screenshot({path:path.join(output,'wishing_well_core_overview.png')});
        await page.click('#wireframe');
        assert.equal((await page.evaluate(()=>ftPreview.snapshot())).wireframe,true);
        await page.click('#wireframe');
        await page.click('[data-model="both"]');
        await page.click('[data-view="perspective"]');
        const baseline = await page.locator('canvas').screenshot();
        await page.locator('canvas').hover();
        await page.mouse.wheel(0,-200);
        await page.waitForTimeout(100);
        assert(!baseline.equals(await page.locator('canvas').screenshot()),'Zoom must change rendered image');
        const zoomed = await page.locator('canvas').screenshot();
        await page.mouse.move(700,420);
        await page.mouse.down();await page.mouse.move(825,455,{steps:10});await page.mouse.up();
        await page.waitForTimeout(100);
        assert(!zoomed.equals(await page.locator('canvas').screenshot()),'Orbit must change rendered image');
        await page.setViewportSize({width:390,height:850});
        await page.waitForTimeout(100);
        assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth));
        await page.screenshot({path:path.join(output,'mobile.png')});
        assert.equal(errors.length,0,errors.join('\n'));
        report.controls=['model-switch','seven-views','wireframe','zoom','orbit','mobile-layout'];
        fs.writeFileSync(path.join(root,'preview-verification.json'),JSON.stringify(report,null,2)+'\n');
        console.log('Verified three model switches, 21 views, orbit/zoom, wireframe, mobile layout; no browser errors.');
    }finally{await browser.close();}
}

async function main(){
    const serving = process.argv.includes('--serve');
    const server = createServer();
    await new Promise((resolve,reject)=>{server.once('error',reject);server.listen(0,'127.0.0.1',resolve);});
    if(serving){console.log('FT model preview: http://127.0.0.1:'+server.address().port);return;}
    try{await verify(server);}finally{await new Promise(resolve=>server.close(resolve));}
}
main().catch(error=>{console.error(error);process.exitCode=1;});
