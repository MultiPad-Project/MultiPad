import { SKIN_DESCRIPTION, SKIN_PREVIEW, SKIN_LINK_DOWNLOAD } from "../constants/skin_page_elements.js";
import { HTML, TITLE } from "../constants/page_body_ids.js";
import {HOST_DIR_KEY} from '/constants/statics.js';

fetch(localStorage.getItem(HOST_DIR_KEY) + + '/page_models/skin.html')
  .then(response => response.text())
  .then(data => {
    var desc = document.getElementById(SKIN_DESCRIPTION);
    var preview = document.getElementById(SKIN_PREVIEW);
    var link = document.getElementById(SKIN_LINK_DOWNLOAD);
    var title = document.getElementById(TITLE);
    document.getElementById(HTML).innerHTML = data;
    document.getElementById(SKIN_DESCRIPTION).textContent = desc.textContent;
    document.getElementById(SKIN_PREVIEW).src = preview.src;
    document.getElementById(SKIN_LINK_DOWNLOAD).href = link.href;
    document.getElementById(TITLE).textContent = title.textContent;
  })
  .catch((error) => {
    console.error('Erro:', error);
  });
  console.log("Finish skin_page.js");
