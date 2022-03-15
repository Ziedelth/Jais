/*
 * Copyright (c) 2021. Ziedelth
 */

const puppeteer = require('puppeteer-extra');
const StealthPlugin = require('puppeteer-extra-plugin-stealth');
const fs = require('fs');

(async () => {
    const myArgs = process.argv.slice(2);
    console.log('myArgs: ', myArgs);

    /* It's a way to check if the user has passed the right number of arguments. */
    if (myArgs.length !== 2) {
        process.exit(0);
        return;
    }

    puppeteer.use(StealthPlugin());

    console.log('Init...');
    /* A way to launch a browser. */
    const browser = await puppeteer.launch({
        headless: true,
        executablePath: '/usr/bin/chromium',
        ignoreHTTPSErrors: true,
        slowMo: 0,
        args: [
            '--window-size=1400,900',
            '--use-gl=egl',
            '--disable-gpu', "--disable-features=IsolateOrigins,site-per-process", '--blink-settings=imagesEnabled=true'
        ],
        defaultViewport: {
            width: 1920,
            height: 1080
        }
    });

    console.log('Go to url...');
    /* It's a way to open a new page. */
    const page = await browser.newPage();
    await page.goto(myArgs[0], {waitUntil: 'load', timeout: 0});

    console.log('Writing file...');
    /* It's a way to write the content of the page to a file. */
    await fs.writeFile('result-' + myArgs[1] + '.html', '<!-- ' + myArgs[0] + ' -->\n' + (await page.content()), async function (err) {
        if (err) throw err;
        console.log('Done!');
        process.exit(0);
    });
})();
