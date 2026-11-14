async page => {
  await page.setViewportSize({width:1280,height:800});
  await page.reload(); await page.waitForTimeout(7000);
  await page.addStyleTag({content:'html,body,*{background:transparent!important;box-shadow:none!important} body *{visibility:hidden!important} #logo3d{visibility:visible!important}'});
  await page.waitForTimeout(400);
  await page.locator('#logo3d').screenshot({path:'still_raw.png',omitBackground:true});
}
