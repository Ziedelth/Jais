/*
 * Copyright (c) 2021. Ziedelth
 */

const puppeteer = require('puppeteer-extra');
const StealthPlugin = require('puppeteer-extra-plugin-stealth');
const fs = require('fs');

index();

async function index() {
    const myArgs = process.argv.slice(2);
    console.log('myArgs: ', myArgs);

    if (myArgs.length !== 1) {
        process.exit(0);
        return;
    }

    puppeteer.use(StealthPlugin());

    const browser = await puppeteer.launch({
        headless: true,
        executablePath: '/usr/bin/chromium-browser',
        devtools: false,
        args: [],
        defaultViewport: {
            width: 1920,
            height: 1080
        }
    });

    const page = await browser.newPage();
    await page.goto(myArgs[0]);

    await fs.writeFile('result.html', (await page.content()), async function (err) {
        if (err) throw err;
        console.log('Done!');
        process.exit(0);
    });
}