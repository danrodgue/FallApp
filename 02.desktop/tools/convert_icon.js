const fs = require('fs');
const path = require('path');
const pngToIco = require('png-to-ico');
const Jimp = require('jimp');

(async () => {
  try {
    const imgDir = path.join(__dirname, '..', 'renderer', 'src', 'img');
    const files = fs.readdirSync(imgDir).filter(f => f.toLowerCase().endsWith('.png'));
    if (!files.length) {
      console.error('No PNG files found in', imgDir);
      process.exit(1);
    }
    // Prefer fallap_logo.png if present
    let srcFile = files.find(f => f.toLowerCase().includes('fallap')) || files[0];
    const src = path.join(imgDir, srcFile);
    const outDir = path.join(__dirname, '..', 'build');
    if (!fs.existsSync(outDir)) fs.mkdirSync(outDir, { recursive: true });
    const tmpPng = path.join(outDir, 'icon-256.png');
    const out = path.join(outDir, 'icon.ico');

    // Resize to 256x256 using Jimp
    const img = await Jimp.read(src);
    img.contain(256, 256, Jimp.HORIZONTAL_ALIGN_CENTER | Jimp.VERTICAL_ALIGN_MIDDLE);
    await img.writeAsync(tmpPng);

    const buf = await pngToIco(tmpPng);
    fs.writeFileSync(out, buf);
    // cleanup tmp
    try { fs.unlinkSync(tmpPng); } catch (e) {}
    console.log('Icon created at', out);
  } catch (err) {
    console.error('Icon conversion failed:', err);
    process.exit(1);
  }
})();
