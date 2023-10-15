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


function handleIndexResult(resultData) {
    console.log("handleMainResult: populating star table from resultData");
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    //let mainTableBodyElement = jQuery("#main_genres");

    // Iterate through resultData, no more than 10 entries
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/index", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleIndexResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
