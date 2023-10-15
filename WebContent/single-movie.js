/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieInfoElement = jQuery("#single_movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>" + resultData[0]["movie_title"] + "(" + resultData[0]["movie_year"] + ")" + "</p>");

    console.log("handleResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#single_movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        console.log(resultData[i]["movie_title"])
        console.log(resultData[i]['movieId'])
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
        rowHTML += "<th>" + parseName(resultData[i]["movie_stars"]) + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th> <form id=\"add_button\" method=\"post\" action=\"api/cart\"><input type=\"hidden\" name=\"mtitle\" value=\"" + resultData[i]["movie_title"]+ "|" + resultData[i]['movieId'] + "\"><button id=\"add\" name=\"add_cart\" type=\"submit\" onclick=alert('Success!')>Add to Cart</button></form></th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}
function parseName(names){
    const splitName = names.split(",");
    let rowHTML = "";
    let n= splitName.length;

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
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id')
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});