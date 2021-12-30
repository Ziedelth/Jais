/*
 * Copyright (c) 2021. Ziedelth
 */

const puppeteer = require('puppeteer-extra');
const StealthPlugin = require('puppeteer-extra-plugin-stealth');
const fs = require('fs');

(async () => {
    const myArgs = process.argv.slice(2);
    console.log('myArgs: ', myArgs);

    if (myArgs.length !== 1) {
        process.exit(0);
        return;
    }

    puppeteer.use(StealthPlugin());

    console.log('Init...');
    const browser = await puppeteer.launch({
        headless: true,
        // executablePath: 'chromium-browser',
        ignoreHTTPSErrors: true,
        slowMo: 0,
        args: [
            '--window-size=1400,900',
            '--disable-gpu', "--disable-features=IsolateOrigins,site-per-process", '--blink-settings=imagesEnabled=true'
        ],
        defaultViewport: {
            width: 1920,
            height: 1080
        }
    });

    console.log('Go to url...');
    const page = await browser.newPage();
    await page.goto(myArgs[0], {waitUntil: 'load', timeout: 0});

    console.log('Writing file...');
    await fs.writeFile('result.html', (await page.content()), async function (err) {
        if (err) throw err;
        console.log('Done!');
        process.exit(0);
    });
})();
