import {CONTENT} from "../constants/page_body_ids.js";

const CONTENTS_DIR = "../contents/";

var content = document.getElementById(CONTENT);
if(content != null){
    
} else {
    console.log("Is null");
}

function LoadContent(current_page_id = CONTENT){
    fetch(current_page_id)
    content.appendChild(

    );
}
