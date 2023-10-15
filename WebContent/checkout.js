

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handlePaymentResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    console.log("handle payment response");

    // If login succeeds, it will redirect the user to confirmation page
    if (resultDataJson["status"] === "success") {
        console.log(resultDataJson)
        window.location.replace("confirmation.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#checkout_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
let payment_form = $("#payment_form");
function submitPaymentForm(formSubmitEvent) {
    console.log("submit payment form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/checkout", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: payment_form.serialize(),
            success: handlePaymentResult
        }
    );
}

// Bind the submit action of the form to a handler function
payment_form.submit(submitPaymentForm);

