#!/usr/bin/env node
/*
 * make_boss_doc.cjs —— 把 docs/boss_reference/*.md 拼成一份 Word(.docx)
 *
 * 为什么手写 OOXML: 本机没有 pandoc / python-docx，也没有联网络装依赖。
 * 一个 .docx 就是个 zip: [Content_Types].xml + _rels/.rels + word/document.xml(+styles/footer)。
 * .NET Framework 的 ZipFile.CreateFromDirectory 在 Windows 上写出来的条目名是反斜杠，
 * Word 打不开 —— 所以这里自己写一个**只存不压(store)** 的 ZIP 打包器，条目名一律 '/'。
 *
 * 支持的 markdown 子集(与子代理约定的一致):
 *   # / ## / ###   标题(H1 强制分页)
 *   - xxx          项目符号
 *   | a | b |      表格(表头 + |---| 分隔行 + 数据行)
 *   普通段落
 *   行内: `代码`  → 等宽字体;  **粗体** → 加粗
 *
 * 用法: node tools/docgen/make_boss_doc.cjs <输出.docx 绝对路径> [源目录] [副标题]
 */
const fs = require('fs');
const path = require('path');

const OUT = process.argv[2];
if (!OUT) {
    console.error('用法: node make_boss_doc.cjs <输出.docx> [源目录] [副标题]');
    process.exit(2);
}
const SRC_DIR = process.argv[3] || path.join(__dirname, '..', '..', 'docs', 'boss_reference');
const SUBTITLE = process.argv[4] || '';

/* ------------------------------------------------------------------ 工具 */

const XML_ESC = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&apos;' };
const esc = (s) => String(s).replace(/[&<>"']/g, (c) => XML_ESC[c]);

/** 行内解析: `code` → 等宽; **bold** → 加粗 */
function runs(text, base) {
    const props = Object.assign({ size: 21, color: '000000', bold: false }, base || {});
    const out = [];
    const re = /\*\*([^*]+)\*\*|`([^`]+)`/g;
    let last = 0, m;
    const push = (t, over) => {
        if (!t) return;
        const p = Object.assign({}, props, over || {});
        const rpr = [];
        rpr.push(`<w:rFonts w:ascii="${p.mono ? 'Consolas' : 'Calibri'}" w:hAnsi="${p.mono ? 'Consolas' : 'Calibri'}" w:eastAsia="微软雅黑"/>`);
        if (p.bold) rpr.push('<w:b/><w:bCs/>');
        if (p.italic) rpr.push('<w:i/>');
        rpr.push(`<w:color w:val="${p.color}"/>`);
        rpr.push(`<w:sz w:val="${p.size}"/><w:szCs w:val="${p.size}"/>`);
        out.push(`<w:r><w:rPr>${rpr.join('')}</w:rPr><w:t xml:space="preserve">${esc(t)}</w:t></w:r>`);
    };
    while ((m = re.exec(text)) !== null) {
        push(text.slice(last, m.index));
        if (m[1] !== undefined) push(m[1], { bold: true });
        else push(m[2], { mono: true, size: (props.size || 21) - 1 });
        last = m.index + m[0].length;
    }
    push(text.slice(last));
    return out.join('');
}

function para(text, opts) {
    const o = opts || {};
    const ppr = [];
    if (o.style) ppr.push(`<w:pStyle w:val="${o.style}"/>`);
    if (o.pageBreak) ppr.push('<w:pageBreakBefore/>');
    if (o.align) ppr.push(`<w:jc w:val="${o.align}"/>`);
    const ind = [];
    if (o.left) ind.push(`w:left="${o.left}"`);
    if (o.hanging) ind.push(`w:hanging="${o.hanging}"`);
    if (ind.length) ppr.push(`<w:ind ${ind.join(' ')}/>`);
    if (o.before !== undefined || o.after !== undefined) {
        ppr.push(`<w:spacing ${o.before !== undefined ? `w:before="${o.before}"` : ''} ${o.after !== undefined ? `w:after="${o.after}"` : ''}/>`);
    }
    const body = text ? runs(text, o.run) : '';
    return `<w:p>${ppr.length ? `<w:pPr>${ppr.join('')}</w:pPr>` : ''}${body}</w:p>`;
}

/** 表格: 第一行当表头 */
function table(rows) {
    const cols = Math.max.apply(null, rows.map((r) => r.length));
    const total = 9638;
    const w = Math.floor(total / cols);
    const grid = Array.from({ length: cols }, () => `<w:gridCol w:w="${w}"/>`).join('');
    const borders = ['top', 'left', 'bottom', 'right', 'insideH', 'insideV']
        .map((s) => `<w:${s} w:val="single" w:sz="4" w:space="0" w:color="8EAADB"/>`).join('');
    const trs = rows.map((cells, ri) => {
        const tcs = [];
        for (let i = 0; i < cols; i++) {
            const txt = cells[i] === undefined ? '' : cells[i];
            const shd = ri === 0 ? '<w:shd w:val="clear" w:color="auto" w:fill="D9E2F3"/>' : '';
            const body = txt ? runs(txt, { size: 20, bold: ri === 0, color: ri === 0 ? '1F3864' : '000000' }) : '';
            tcs.push(`<w:tc><w:tcPr><w:tcW w:w="${w}" w:type="dxa"/>${shd}<w:vAlign w:val="center"/></w:tcPr>`
                + `<w:p><w:pPr><w:spacing w:before="20" w:after="20"/></w:pPr>${body}</w:p></w:tc>`);
        }
        return `<w:tr>${ri === 0 ? '<w:trPr><w:tblHeader/></w:trPr>' : ''}${tcs.join('')}</w:tr>`;
    }).join('');
    return `<w:tbl><w:tblPr><w:tblW w:w="5000" w:type="pct"/><w:tblBorders>${borders}</w:tblBorders>`
        + `<w:tblLayout w:type="fixed"/><w:tblCellMar><w:left w:w="80" w:type="dxa"/><w:right w:w="80" w:type="dxa"/></w:tblCellMar></w:tblPr>`
        + `<w:tblGrid>${grid}</w:tblGrid>${trs}</w:tbl>${para('', { after: 60 })}`;
}

/* ------------------------------------------------------- markdown → OOXML */

function convert(md) {
    const lines = md.replace(/\r\n?/g, '\n').split('\n');
    const out = [];
    let i = 0;
    while (i < lines.length) {
        const raw = lines[i];
        const line = raw.trim();
        if (!line) { i++; continue; }

        if (line.startsWith('|')) {                       // 表格
            const rows = [];
            while (i < lines.length && lines[i].trim().startsWith('|')) {
                const cells = lines[i].trim().replace(/^\|/, '').replace(/\|$/, '').split('|').map((c) => c.trim());
                if (!cells.every((c) => /^:?-{2,}:?$/.test(c) || c === '')) rows.push(cells);
                i++;
            }
            if (rows.length) out.push(table(rows));
            continue;
        }
        if (/^###\s+/.test(line)) { out.push(para(line.replace(/^###\s+/, ''), { style: 'Heading3' })); i++; continue; }
        if (/^##\s+/.test(line)) { out.push(para(line.replace(/^##\s+/, ''), { style: 'Heading2' })); i++; continue; }
        if (/^#\s+/.test(line)) { out.push(para(line.replace(/^#\s+/, ''), { style: 'Heading1' })); i++; continue; }
        if (/^[-*]\s+/.test(line)) { out.push(para('• ' + line.replace(/^[-*]\s+/, ''), { left: 340, hanging: 200, after: 40 })); i++; continue; }
        if (/^>\s?/.test(line)) { out.push(para(line.replace(/^>\s?/, ''), { run: { italic: true, color: '595959' }, left: 340, after: 60 })); i++; continue; }
        if (/^---+$/.test(line)) { i++; continue; }
        out.push(para(line, { after: 80 }));
        i++;
    }
    return out.join('');
}

/* ------------------------------------------------------------ 文档骨架 */

const STATIC = {
    contentTypes: `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
<Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/>
</Types>`,
    rels: `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>`,
    docRels: `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/>
</Relationships>`,
    styles: `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:docDefaults><w:rPrDefault><w:rPr>
<w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="微软雅黑" w:cs="Calibri"/>
<w:color w:val="000000"/><w:sz w:val="21"/><w:szCs w:val="21"/>
</w:rPr></w:rPrDefault><w:pPrDefault><w:pPr><w:spacing w:after="80" w:line="288" w:lineRule="auto"/></w:pPr></w:pPrDefault></w:docDefaults>
<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/></w:style>
<w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:basedOn w:val="Normal"/><w:qFormat/>
<w:pPr><w:jc w:val="center"/><w:spacing w:before="240" w:after="120"/></w:pPr>
<w:rPr><w:rFonts w:eastAsia="微软雅黑"/><w:b/><w:color w:val="1F3864"/><w:sz w:val="56"/><w:szCs w:val="56"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Subtitle"><w:name w:val="Subtitle"/><w:basedOn w:val="Normal"/><w:qFormat/>
<w:pPr><w:jc w:val="center"/><w:spacing w:after="240"/></w:pPr>
<w:rPr><w:rFonts w:eastAsia="微软雅黑"/><w:color w:val="595959"/><w:sz w:val="26"/><w:szCs w:val="26"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/>
<w:pPr><w:pageBreakBefore/><w:keepNext/><w:spacing w:before="120" w:after="160"/><w:outlineLvl w:val="0"/>
<w:pBdr><w:bottom w:val="single" w:sz="12" w:space="4" w:color="2F5496"/></w:pBdr></w:pPr>
<w:rPr><w:rFonts w:eastAsia="微软雅黑"/><w:b/><w:color w:val="1F3864"/><w:sz w:val="40"/><w:szCs w:val="40"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/>
<w:pPr><w:keepNext/><w:spacing w:before="220" w:after="100"/><w:outlineLvl w:val="1"/></w:pPr>
<w:rPr><w:rFonts w:eastAsia="微软雅黑"/><w:b/><w:color w:val="2F5496"/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="heading 3"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:qFormat/>
<w:pPr><w:keepNext/><w:spacing w:before="160" w:after="60"/><w:outlineLvl w:val="2"/></w:pPr>
<w:rPr><w:rFonts w:eastAsia="微软雅黑"/><w:b/><w:color w:val="375623"/><w:sz w:val="25"/><w:szCs w:val="25"/></w:rPr></w:style>
</w:styles>`,
    footer: `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:p><w:pPr><w:jc w:val="center"/></w:pPr>
<w:r><w:rPr><w:rFonts w:eastAsia="微软雅黑"/><w:color w:val="808080"/><w:sz w:val="18"/></w:rPr><w:t xml:space="preserve">FT-Boss 数值与机制速查  ·  第 </w:t></w:r>
<w:r><w:fldChar w:fldCharType="begin"/></w:r>
<w:r><w:instrText xml:space="preserve"> PAGE </w:instrText></w:r>
<w:r><w:fldChar w:fldCharType="separate"/></w:r>
<w:r><w:t>1</w:t></w:r>
<w:r><w:fldChar w:fldCharType="end"/></w:r>
<w:r><w:rPr><w:rFonts w:eastAsia="微软雅黑"/><w:color w:val="808080"/><w:sz w:val="18"/></w:rPr><w:t xml:space="preserve"> 页</w:t></w:r>
</w:p></w:ftr>`
};

/* --------------------------------------------------------------- 打包 ZIP */

const CRC_TABLE = (() => {
    const t = new Int32Array(256);
    for (let n = 0; n < 256; n++) {
        let c = n;
        for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
        t[n] = c;
    }
    return t;
})();

function crc32(buf) {
    let c = -1;
    for (let i = 0; i < buf.length; i++) c = CRC_TABLE[(c ^ buf[i]) & 0xFF] ^ (c >>> 8);
    return (c ^ -1) >>> 0;
}

function zip(entries) {
    const chunks = [], central = [];
    let offset = 0;
    const now = new Date();
    const dosTime = ((now.getHours() << 11) | (now.getMinutes() << 5) | (now.getSeconds() / 2)) & 0xFFFF;
    const dosDate = (((now.getFullYear() - 1980) << 9) | ((now.getMonth() + 1) << 5) | now.getDate()) & 0xFFFF;
    for (const [name, content] of entries) {
        const nameBuf = Buffer.from(name, 'utf8');
        const data = Buffer.isBuffer(content) ? content : Buffer.from(content, 'utf8');
        const crc = crc32(data);
        const lh = Buffer.alloc(30);
        lh.writeUInt32LE(0x04034b50, 0); lh.writeUInt16LE(20, 4); lh.writeUInt16LE(0x0800, 6);
        lh.writeUInt16LE(0, 8); lh.writeUInt16LE(dosTime, 10); lh.writeUInt16LE(dosDate, 12);
        lh.writeUInt32LE(crc, 14); lh.writeUInt32LE(data.length, 18); lh.writeUInt32LE(data.length, 22);
        lh.writeUInt16LE(nameBuf.length, 26); lh.writeUInt16LE(0, 28);
        chunks.push(lh, nameBuf, data);
        const ch = Buffer.alloc(46);
        ch.writeUInt32LE(0x02014b50, 0); ch.writeUInt16LE(20, 4); ch.writeUInt16LE(20, 6);
        ch.writeUInt16LE(0x0800, 8); ch.writeUInt16LE(0, 10); ch.writeUInt16LE(dosTime, 12);
        ch.writeUInt16LE(dosDate, 14); ch.writeUInt32LE(crc, 16);
        ch.writeUInt32LE(data.length, 20); ch.writeUInt32LE(data.length, 24);
        ch.writeUInt16LE(nameBuf.length, 28); ch.writeUInt16LE(0, 30); ch.writeUInt16LE(0, 32);
        ch.writeUInt16LE(0, 34); ch.writeUInt16LE(0, 36); ch.writeUInt32LE(0, 38);
        ch.writeUInt32LE(offset, 42);
        central.push(ch, nameBuf);
        offset += lh.length + nameBuf.length + data.length;
    }
    const cdBuf = Buffer.concat(central);
    const eocd = Buffer.alloc(22);
    eocd.writeUInt32LE(0x06054b50, 0); eocd.writeUInt16LE(0, 4); eocd.writeUInt16LE(0, 6);
    eocd.writeUInt16LE(entries.length, 8); eocd.writeUInt16LE(entries.length, 10);
    eocd.writeUInt32LE(cdBuf.length, 12); eocd.writeUInt32LE(offset, 16); eocd.writeUInt16LE(0, 20);
    return Buffer.concat([...chunks, cdBuf, eocd]);
}

/* ------------------------------------------------------------------ 主流程 */

const files = fs.readdirSync(SRC_DIR).filter((f) => f.toLowerCase().endsWith('.md')).sort();
if (!files.length) {
    console.error('没有找到 markdown 源: ' + SRC_DIR);
    process.exit(1);
}

const body = [];
const toc = [];
const stamp = new Date().toLocaleString('zh-CN', { hour12: false });

// 封面
body.push(para('FT-Boss 全 Boss 数值与技能机制速查', { style: 'Title' }));
body.push(para(SUBTITLE || '模组 ftboss', { style: 'Subtitle' }));
body.push(para('生成时间：' + stamp, { align: 'center', run: { color: '808080', size: 20 }, after: 200 }));
body.push(para('本文档由代码自动整理生成，数值口径与说明', { after: 40 }));
body.push(para('• 全部数字取自工程源码里的常量/方法，条目后括号内是出处；代码里查不到的会标「?」，没有猜测值。', { left: 340, hanging: 200, after: 40 }));
body.push(para('• 伤害一律写代码里的**字面常量**。实际打到玩家身上的值以 BOSS_DAMAGE_MULT（=4/3）等全局系数结算，需要时条目里会注明「×全局倍率」。', { left: 340, hanging: 200, after: 40 }));
body.push(para('• 时间单位：1 秒 = 20 tick（代码里写 100 = 5 秒）；距离单位：1 格 = 1 block。', { left: 340, hanging: 200, after: 40 }));
body.push(para('• 重新生成：改完代码后跑 node tools/docgen/make_boss_doc.cjs <输出路径> 即可刷新本文档。', { left: 340, hanging: 200, after: 200 }));

// 手工目录
body.push(para('本文档包含的小节', { style: 'Heading2' }));
for (const f of files) {
    // 注意：源文件可能是 UTF-8 BOM 开头，必须先 trim 掉 \uFEFF，
    // 否则 /^#\s+/ 匹配不到第一行，目录项会退化成文件名。
    const first = (fs.readFileSync(path.join(SRC_DIR, f), 'utf8').replace(/^\uFEFF/, '')
        .split('\n').map((l) => l.trim()).find((l) => /^#\s+/.test(l)) || '').replace(/^#\s+/, '').trim();
    const title = first || f.replace(/\.md$/, '');
    toc.push(title);
    body.push(para('• ' + title, { left: 340, hanging: 200, after: 30 }));
}
body.push(para('', {}));

for (const f of files) {
    const md = fs.readFileSync(path.join(SRC_DIR, f), 'utf8');
    body.push(convert(md));
}

const documentXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
<w:body>${body.join('')}
<w:sectPr><w:footerReference w:type="default" r:id="rId2"/>
<w:pgSz w:w="11906" w:h="16838"/>
<w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134" w:header="851" w:footer="992" w:gutter="0"/>
</w:sectPr></w:body></w:document>`;

const buf = zip([
    ['[Content_Types].xml', STATIC.contentTypes],
    ['_rels/.rels', STATIC.rels],
    ['word/document.xml', documentXml],
    ['word/_rels/document.xml.rels', STATIC.docRels],
    ['word/styles.xml', STATIC.styles],
    ['word/footer1.xml', STATIC.footer]
]);

fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(OUT, buf);
console.log('[ok] ' + OUT);
console.log('     源文件 ' + files.length + ' 个，约 ' + Math.round(Buffer.byteLength(documentXml) / 1024) + ' KB XML，docx ' + Math.round(buf.length / 1024) + ' KB');
console.log('     目录: ' + toc.join(' / '));
