import { SKINS_LIST, SKINS_LIST_ITEM, SKINS_LIST_ITEM_IMG } from "../constants/page_body_ids.js";
import { SKIN_DESCRIPTION } from "../constants/skin_page_elements.js";

fetch("../skins/skins.json")
    .then(response => response.json())
    .then(data => {
        var list = document.getElementById(SKINS_LIST);
        data.skins.forEach(item => {
            var list_div = document.createElement('div');
            var list_item = document.createElement('a');
            list_div.appendChild(list_item);
            var item_img = document.createElement('img');
            var item_title = document.createElement('p');
            var item_desc = document.createElement('p1');

            list_item.href = item.link_download;
            list_item.className = SKINS_LIST_ITEM;
            item_desc.innerHTML = item.description;
            item_desc.className = SKIN_DESCRIPTION;
            item_img.src = item.preview_image;
            item_img.className = SKINS_LIST_ITEM_IMG;
            item_title.innerHTML = item.name + " by <a href=\"" + item.author_link + "\">" + item.author + "</a>"
        
            list_item.appendChild(item_img);
            list_item.appendChild(item_title);
            list_item.appendChild(item_desc)
            list.appendChild(list_div);
        });
    })
    .catch(error => console.error(error));
    console.log("Finish skins.js");
