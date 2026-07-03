let cartModalTimeout = null;

function openProductModal(id, name, price, image, description) {
    document.getElementById('modal-p-id').value = id;
    document.getElementById('modal-p-name').innerText = name;
    document.getElementById('modal-p-image').src = image;
    document.getElementById('modal-p-qty').value = 1;
    document.getElementById('modal-p-desc').innerText = description;
    document.getElementById('modal-p-price').innerText = new Intl.NumberFormat('vi-VN').format(price) + " VNĐ";
    document.getElementById('product-detail-modal').style.display = 'block';
}

function openProductModalFromData(element) {
    const id = element.getAttribute('data-id');
    const name = element.getAttribute('data-name');
    const price = parseFloat(element.getAttribute('data-price'));
    const image = element.getAttribute('data-image');
    const description = element.getAttribute('data-desc');
    openProductModal(id, name, price, image, description);
}

function closeProductModal() {
    document.getElementById('product-detail-modal').style.display = 'none';
}

function triggerAddToCartFromModal() {
    const productId = document.getElementById('modal-p-id').value;
    const qtyInput = document.getElementById('modal-p-qty');

    if (!qtyInput) return;
    const quantity = parseInt(qtyInput.value);
    closeProductModal();
    executeAddToCartAJAX(productId, quantity);
}

function executeAddToCartAJAX(productId, quantity) {
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const url = (contextPath === "" || contextPath === "/") ? "/cart/add" : `${contextPath}/cart/add`;

    fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
        credentials: 'include',
        body: `productId=${productId}&quantity=${quantity}`
    })
        .then(response => response.json().then(data => {
            if (!response.ok) throw new Error(data.status || 'CONNECTION_ERROR');
            return data;
        }))
        .then(data => {
            if (data.status === 'success') {
                const cartBadge = document.getElementById('cart-badge');
                if (cartBadge) cartBadge.innerText = data.totalCount;

                const modal = document.getElementById('cart-modal');
                if (modal) {
                    modal.style.display = 'block';
                    if (cartModalTimeout) clearTimeout(cartModalTimeout);
                    cartModalTimeout = setTimeout(() => continueShopping(), 3000);
                }
            }
        })
        .catch(error => {
            switch (error.message) {
                case 'INVALID_QUANTITY': alert("Số lượng mua phải là số dương"); break;
                case 'STOCK_EXCEEDED': alert("Chỉ còn đủ sản phẩm trong hệ thống, vui lòng kiểm tra lại số lượng"); break;
                default: alert("Không thể cập nhật giỏ hàng lúc này"); break;
            }
        });
}

function continueShopping() {
    const modal = document.getElementById('cart-modal');
    if (modal) modal.style.display = 'none';
    if (cartModalTimeout) clearTimeout(cartModalTimeout);
}

function viewCart() {
    if (cartModalTimeout) clearTimeout(cartModalTimeout);
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    window.location.href = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;
}

function deleteItemAjax(productId) {
    if (!confirm("Bạn có chắc chắn muốn xóa sản phẩm này không?")) return;

    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const baseUrl = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;

    const params = new URLSearchParams();
    params.append("action", "DELETE");
    params.append("productId", productId);

    fetch(baseUrl, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
        credentials: 'include',
        body: params.toString()
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                const row = document.getElementById(`row-${productId}`);
                if (row) row.remove();
                document.getElementById("total-amount").innerText = new Intl.NumberFormat('vi-VN').format(data.newTotal);
                if (document.querySelectorAll(".cart-table tbody tr").length === 0) window.location.reload();
            }
        });
}

function updateQuantityAjax(productId, inputElement) {
    const newQty = parseInt(inputElement.value);
    const originalQty = parseInt(inputElement.getAttribute("data-last-valid"));

    if (newQty <= 0) { deleteItemAjax(productId); return; }

    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const baseUrl = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;

    fetch(`${baseUrl}?queryType=checkStock&productId=${productId}&newQty=${newQty}`, {
        method: "POST",
        credentials: 'include'
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === "outOfStock") {
                alert(`Hệ thống chỉ còn ${data.maxAvailable} sản phẩm.`);
                inputElement.value = originalQty;
                return;
            }

            const params = new URLSearchParams();
            params.append("action", "UPDATE");
            params.append("productId", productId);
            params.append("newQty", newQty);

            fetch(baseUrl, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
                credentials: 'include',
                body: params.toString()
            })
                .then(res => res.json())
                .then(resData => {
                    if (resData.success) {
                        inputElement.setAttribute("data-last-valid", newQty);
                        document.getElementById(`total-item-${productId}`).innerText = new Intl.NumberFormat('vi-VN').format(resData.itemTotal) + " VNĐ";
                        document.getElementById("total-amount").innerText = new Intl.NumberFormat('vi-VN').format(resData.cartTotal);
                    }
                });
        });
}

function toggleSelectionAjax(productId, checkboxElement) {
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const baseUrl = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;

    const params = new URLSearchParams();
    params.append("action", "SELECT");
    params.append("productId", productId);
    params.append("isChecked", checkboxElement.checked);

    fetch(baseUrl, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
        credentials: 'include',
        body: params.toString()
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                document.getElementById("total-amount").innerText = new Intl.NumberFormat('vi-VN').format(data.cartTotal);
            }
        });
}

function proceedToCheckout(event) {
    event.preventDefault();
    if (document.querySelectorAll(".item-checkbox:checked").length === 0) {
        alert("Vui lòng tích chọn ít nhất một sản phẩm!");
        return;
    }

    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const baseUrl = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;

    fetch(`${baseUrl}?queryType=checkAuth`, {
        method: "POST",
        credentials: 'include'
    })
        .then(res => res.json())
        .then(data => {
            if (data.isGuest) {
                alert("Bạn cần đăng nhập trước khi đặt hàng!");
                window.location.href = `${contextPath}/login`;
            } else {
                document.getElementById('cart-container').style.display = 'none';
                document.getElementById('order-info-container').style.display = 'block';
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        });
}