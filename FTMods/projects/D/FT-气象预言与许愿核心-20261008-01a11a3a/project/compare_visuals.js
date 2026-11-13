const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const sharp = require('sharp');
const {chromium} = require('playwright');

const root = __dirname;
const base = process.env.FT_PREVIEW_TEST_URL || 'http://127.0.0.1:18790/instruments/viewer.html';
const baselineRoot = path.join(root,'revisions/superseded-preoptimization');
const output = path.join(root,'optimization/visual-comparison');

async function frames(page,label){
    const images=[];
    for(const name of ['weather_forecaster','oracle','wishing_well_core']){
        await page.evaluate(model=>{ftPreview.setPlaying(false);ftPreview.setAvatar(false);ftPreview.setModel(model);},name);
        const states=['perspective','front','back','left','right','top','bottom'].map(view=>({view,time:0}));
        states.push(...[1.5,3,4.5,6,7.5,9,10.5].map(time=>({view:'perspective',time})));
        for(const state of states){
            await page.evaluate(({view,time})=>{ftPreview.setView(view);ftPreview.setAnimationTime(time);},state);
            await page.waitForTimeout(55);
            const buffer=await page.locator('canvas').screenshot();
            const filename=name+'_'+state.view+'_'+state.time;
            fs.writeFileSync(path.join(output,label+'_'+filename+'.png'),buffer);
            images.push({name,state,filename,buffer});
        }
    }
    return images;
}

async function main(){
    fs.mkdirSync(output,{recursive:true});
    const browser=await chromium.launch({channel:'msedge',headless:true,args:['--enable-webgl','--use-angle=swiftshader','--enable-unsafe-swiftshader']});
    const page=await browser.newPage({viewport:{width:1280,height:1000},deviceScaleFactor:1});
    const errors=[];
    page.on('pageerror',error=>errors.push(error.message));
    page.on('response',response=>{if(response.status()>=400)errors.push(response.url());});
    try{
        const pattern='**/payload/assets/**/models/block/*.json';
        await page.route(pattern,route=>{
            const pathname=new URL(route.request().url()).pathname;
            const relative=pathname.slice(pathname.indexOf('/payload/')+1);
            route.fulfill({status:200,contentType:'application/json',body:fs.readFileSync(path.join(baselineRoot,relative))});
        });
        await page.goto(base);
        await page.waitForFunction(()=>window.ftPreviewReady);
        const before=await frames(page,'before');
        await page.unroute(pattern);
        await page.reload();await page.waitForFunction(()=>window.ftPreviewReady);
        const after=await frames(page,'after');
        const cases=[];
        for(const [index,original] of before.entries()){
            const previous=await sharp(original.buffer).ensureAlpha().raw().toBuffer({resolveWithObject:true});
            const current=await sharp(after[index].buffer).ensureAlpha().raw().toBuffer({resolveWithObject:true});
            assert.deepEqual(previous.info,current.info);
            let changedPixels=0,maxDifference=0;
            for(let offset=0;offset<previous.data.length;offset+=4){
                let difference=0;
                for(let channel=0;channel<3;channel++)difference=Math.max(difference,Math.abs(previous.data[offset+channel]-current.data[offset+channel]));
                maxDifference=Math.max(maxDifference,difference);
                if(difference>3)changedPixels++;
            }
            const changedFraction=changedPixels/(previous.info.width*previous.info.height);
            cases.push({name:original.name,...original.state,changedPixels,changedFraction,maxDifference});
            assert(changedFraction<=.0001,'Visible surface regression: '+original.filename+' changed fraction '+changedFraction);
        }
        assert.equal(errors.length,0,errors.join('\n'));
        const report={testType:'offline-WebGL-comparison-not-Minecraft',base,cases,caseCount:cases.length,unchangedVisibleAppearance:true,pixelThreshold:3,maxAllowedChangedFraction:.0001,errors};
        fs.writeFileSync(path.join(root,'optimization/visual-comparison.json'),JSON.stringify(report,null,2)+'\n');
        console.log('Visual comparison passed:',cases.length,'views/turntable angles; max changed-pixel fraction',Math.max(...cases.map(entry=>entry.changedFraction)));
    }finally{await browser.close();}
}
main().catch(error=>{console.error(error.stack);process.exitCode=1;});
