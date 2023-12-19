import { CONTENT, TITLE, TAB_TITLE, HTML } from "../constants/page_body_ids.js";
import {HOST_DIR_KEY} from '/constants/statics.js';

fetch(localStorage.getItem(HOST_DIR_KEY) + '/page_models/default.html')
  .then(response => response.text())
  .then(data => {
    let content = document.getElementById(CONTENT);
    let title = document.getElementById(TITLE);
    let tab_title = document.getElementById(TAB_TITLE);
    document.getElementById(HTML).innerHTML = data;
    document.getElementById(CONTENT).replaceWith(content);
    document.getElementById(TITLE).textContent = title.textContent;
    document.getElementById(TAB_TITLE).replaceWith(tab_title);
    let scripts = document.getElementsByTagName('script');
    for(let i = 0; i < scripts.length; i++){
      eval(scripts[i].innerHTML);
    }
  })
  .catch((error) => {
    console.error('Erro:', error);
  });
console.log("Finish content.js");
