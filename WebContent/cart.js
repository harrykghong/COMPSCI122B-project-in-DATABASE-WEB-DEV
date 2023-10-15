let cart = $("#cart");

/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle session response");
    // console.log(resultDataJson);
    // console.log(resultDataJson["sessionID"]);

    // show the session information 
    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);

    // show cart information
    handleCartArray(resultDataJson["previousItems"]);
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultMap) {
    let cartTableBodyElement = jQuery("#cart_table_body");
    // change it to html list
    cartTableBodyElement.html("");
    for (const key in resultMap) {
        const value = resultMap[key].first
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td>" + key + "</td>";
        rowHTML += '<td><button class="minus-button">-</button><span class="quantity-value">' + value + '</span><button class="plus-button">+</button></td>';
        rowHTML += '<td><button class="delete-button">Delete</button></td>';
        rowHTML += "<td class='price'>" + "10.00" + "</td>";
        rowHTML += "<td class='total'>" + (10.00 * value).toFixed(2) + "</td>";
        rowHTML += "</tr>";
        cartTableBodyElement.append(rowHTML);
    }
    updateCartTotal()
}
function updateTotal(row, quantity) {
    const price = parseFloat(row.find('.price').text());
    const total = quantity * price;
    row.find('.total').text(total.toFixed(2));
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
function updateQuantity(row, delta) {
    const movieTitle = row.find('td:first-child').text();
    const quantitySpan = row.find('.quantity-value');
    const currentQuantity = parseInt(quantitySpan.text());
    const newQuantity = currentQuantity + delta;
    if (newQuantity > 0) {
        quantitySpan.text(newQuantity);
        updateTotal(row, newQuantity);
        $.ajax("api/cart", {
            method: "POST",
            data: {
                action: delta > 0 ? 'add' : 'decrease',
                mtitle: movieTitle
            }
        });
    }
    updateCartTotal();
}

function onMinusButtonClick(event) {
    const button = $(event.currentTarget);
    const row = button.closest('tr');
    updateQuantity(row, -1);
}

function onPlusButtonClick(event) {
    const button = $(event.currentTarget);
    const row = button.closest('tr');
    updateQuantity(row, 1);
}

function onDeleteButtonClick(event) {
    const button = $(event.currentTarget);
    const row = button.closest('tr');
    const m_title = row.find('td:first-child').text();
    console.log(m_title)
    row.remove();
    updateCartTotal();
    $.ajax("api/cart", {
        method: "POST",
        data: {
            action: 'delete',
            mtitle: m_title
        }
    });
}

$(document).on('click', '.minus-button', onMinusButtonClick);
$(document).on('click', '.plus-button', onPlusButtonClick);
$(document).on('click', '.delete-button', onDeleteButtonClick);
$.ajax("api/cart", {
    method: "GET",
    success: handleSessionData
});

