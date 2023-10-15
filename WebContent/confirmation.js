let cart = $("#cart");

/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    console.log("handle session response");
    console.log(resultDataJson)
    // show cart information
    handleCartArray(resultDataJson);
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultMap) {
    let cartTableBodyElement = jQuery("#cart_table_body");
    // change it to html list
    cartTableBodyElement.html("");
    let i = 0;

    for (const key in resultMap["previousItems"]) {
        const value = resultMap["previousItems"][key].first
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += '<td>' + resultMap["auto_incremented"][i] + '</td>';
        rowHTML += "<td>" + key + "</td>";
        rowHTML += "<td>" + value + "</td>";
        rowHTML += "<td class='price'>" + "10.00" + "</td>";
        rowHTML += "<td class='total'>" + (10.00 * value).toFixed(2) + "</td>";
        rowHTML += "</tr>";
        cartTableBodyElement.append(rowHTML);
        i++;
    }
    updateCartTotal()
}
function updateCartTotal() {
    let totalPrice = 0;

    // Iterate through each row in the cart table
    $('#cart_table_body tr').each(function() {
        // Get the total price of the current row and add it to the totalPrice variable
        const rowTotal = parseFloat($(this).find('.total').text());
        totalPrice += rowTotal;
    });

    // Update the total price element with the new total price
    $('#total-price').text('Total: $' + totalPrice.toFixed(2));
}
$.ajax("api/cart", {
    method: "GET",
    success: handleSessionData
});

