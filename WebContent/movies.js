/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */


function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
function handleStarResult(resultData) {
    console.log("handleStarResult: populating movies table from resultData");
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 1; i < Math.min(101, resultData.length); i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movieId'] + '">'
            + resultData[i]["movieName"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movieYear"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movieDir"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movieGen"] + "</th>";
        rowHTML += "<th>" + parseName(resultData[i]["movieStar"]) + "</th>";
        rowHTML += "<th>" + resultData[i]["movieRate"] + "</th>";
        rowHTML += "<th> <form id=\"add_button\" method=\"post\" action=\"api/cart\"><input type=\"hidden\" name=\"mtitle\" value=\"" + resultData[i]["movieName"]+ "|" + resultData[i]['movieId'] + "\"><button id=\"add\" name=\"add_cart\" type=\"submit\" onclick=alert('Success!')>Add to Cart</button></form></th>";
        rowHTML += "</tr>";
        //parseName(resultData[i]["movieStar"], resultData[i]["movieStarId"])
        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
    //change option to make result consistent
    const $select1 = document.querySelector('#pageNum');
    $select1.value = resultData[0]["pageNum"];
    const $select2 = document.querySelector('#sort');
    $select2.value = resultData[0]["sort"];


    let pageInfo = jQuery("#current_page_num");
    let html = "Page "+ resultData[0]["page"] +" of "+ resultData[0]["maxPage"] + " Page, "+ resultData[0]["count"] +" results.";
    pageInfo.append(html);
    if(resultData[0]["page"]<=1){
        document.querySelector('#pre').disabled = true;
    }else{
        document.querySelector('#pre').disabled = false;
    }
    if(resultData[0]["page"]>=resultData[0]["maxPage"]){
        document.querySelector('#next').disabled = true;
    }else{
        document.querySelector('#next').disabled = false;
    }

    console.log("finished");
}

// function updateTable(){
//     PageMethod
//     console.log("success");
// }


function parseName(names){
    const splitName = names.split(",");
    let rowHTML = "";
    let n=0;
    if(splitName.length>=3){
        n = 3;
    }else{
        n = splitName.length;
    }
    for(let j = 0; j<n; j++){
        rowHTML+='<a href="single-star.html?id=' + splitName[j].slice(-9)+ '">'+ splitName[j].slice(0,-10) +'</a>';
        //rowHTML+=splitName[j];
        if(j<(n-1)){
            rowHTML+=", ";
        }
    }
    return rowHTML;
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
let movieGenre = getParameterByName('genre')
let moviePrefix = getParameterByName('prefix')
let movieTitle = getParameterByName('title')
let fullMovieTitle = getParameterByName('fullTitle')
let movieYear = getParameterByName('year')
let movieDir = getParameterByName('dir')
let movieStar = getParameterByName('star')
let pageNum = getParameterByName('pageNum')
let sort = getParameterByName('sort')
let changePage = getParameterByName('changePage')

console.log("movieTitle = " + movieTitle)
console.log("movieYear = " + movieYear)
console.log("movieDir = " + movieDir)
console.log("movieStar = " + sort)
temp_url = "api/movies?placeholder="

if(movieGenre != null){
    temp_url += "&genre=" + movieGenre
}
if(moviePrefix != null){
    temp_url += "&prefix=" + moviePrefix
}
if(movieTitle != null){
    temp_url += "&title=" + movieTitle
}
if(fullMovieTitle != null){
    temp_url += "&fullTitle=" + fullMovieTitle
}
if(movieYear != null){
    temp_url += "&year=" + movieYear
}
if(movieDir != null){
    temp_url += "&dir=" + movieDir
}
if(movieStar != null){
    temp_url += "&star=" + movieStar
}
if(pageNum != null){
    temp_url += "&pageNum=" + pageNum
}
if(sort != null){
    temp_url += "&sort=" + sort
}
if(changePage != null){
    temp_url += "&changePage=" + changePage
}
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: temp_url, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});


$("#add").click(function() {
    $.ajax({
        type: "POST",
        url: "api/cart",
        data: $("#add_button").serialize(), // serializes the form's elements.
        success: function(data)
        {
        }
    });
    return false; // avoid to execute the actual submit of the form.
});