import { CONTENT, TITLE } from "../constants/page_body_ids.js";

fetch('../page_models/default.html')
  .then(response => response.text())
  .then(data => {
    var content = document.getElementById(CONTENT);
    var title = document.getElementById(TITLE);
    document.body.outerHTML = data;
    document.getElementById(CONTENT).replaceWith(content);
    document.getElementById(TITLE).replaceWith(title);
  })
  .catch((error) => {
    console.error('Erro:', error);
  });
