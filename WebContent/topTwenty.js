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
function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

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
        rowHTML += "</tr>";
        //parseName(resultData[i]["movieStar"], resultData[i]["movieStarId"])
        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}

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

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});