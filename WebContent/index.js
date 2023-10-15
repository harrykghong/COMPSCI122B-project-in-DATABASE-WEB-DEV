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

let map = new Map();

function handleIndexResult(resultData) {
    console.log("handleMainResult: populating genre table from resultData");
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let genreTableBodyElement = jQuery("#genre_table_body");

    // Iterate through resultData, no more than 10 entries
    let rowHTML = ""; //if here
    for (let i = 0; i < resultData.length; i++) {
        // Concatenate the html tags with resultData jsonObject
        if(i%4==0){
            rowHTML += "<tr>";
        }
        rowHTML +=
            "<td>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="movies.html?genre=' + resultData[i]['genre_name'] + '">'
            + resultData[i]["genre_name"] +     // display star_name for the link text
            '</a>' +
            "</td>";
        if(i%4==3 || i==(resultData.length-1)){
            rowHTML += "</tr>";
        }
    }
    genreTableBodyElement.append(rowHTML);
}


function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated");
    if(query.length<3){
        console.log("Query too short");
        handleLookupAjaxSuccess({}, query, doneCallback);
        return;
    }
    if(map.get(query)!=undefined){
        console.log("Using Cached Result");
        handleLookupAjaxSuccess(map.get(query), query, doneCallback);
        return;
    }
    // TODO: if you want to check past query results first, you can do it here

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "api/autocomplete?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            console.log("Sending Ajax Request")
            map.set(query, data);
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("Suggestion List: ");
    console.log(data);
    // parse the string into JSON
    // let jsonData = JSON.parse(data);
    // console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    window.location.replace("single-movie.html?id="+suggestion["data"]["movieId"]);
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#fullSearch').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here
}

// bind pressing enter key to a handler function
$('#fullSearch').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#fullSearch').val())
    }
})



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
