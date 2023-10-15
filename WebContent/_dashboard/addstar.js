

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    console.log(resultDataString);
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // Create a new list item
    let li = document.createElement("li");
    let text = document.createTextNode(resultDataJson["message"]);
    li.appendChild(text);
    // Add the new list item to the existing list
    let myList = document.getElementById("addstar_message");
    myList.appendChild(li);
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
let addstar_form = $("#addstar_form");
function submitLoginForm(formSubmitEvent) {

    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/addstar", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: addstar_form.serialize(),
            success: handleLoginResult
        }
    );
}

// Bind the submit action of the form to a handler function
addstar_form.submit(submitLoginForm);

