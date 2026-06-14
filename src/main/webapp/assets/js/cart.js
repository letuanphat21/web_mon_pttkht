
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
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        body: `productId=${productId}&quantity=${quantity}`
    })
        .then(response => {
            return response.json().then(data => {
                if (!response.ok) {
                    throw new Error(data.status || 'CONNECTION_ERROR');
                }
                return data;
            });
        })
        .then(data => {
            if (data.status === 'success') {
                const cartBadge = document.getElementById('cart-badge');
                if (cartBadge) {
                    cartBadge.innerText = data.totalCount;
                }

                const modal = document.getElementById('cart-modal');
                if (modal) {
                    modal.style.display = 'block';
                    if (cartModalTimeout) clearTimeout(cartModalTimeout);
                    cartModalTimeout = setTimeout(() => {
                        continueShopping();
                    }, 3000);
                }
            }
        })
        .catch(error => {
            switch (error.message) {
                case 'INVALID_QUANTITY':
                    alert("Số lượng mua phải là số dương");
                    break;
                case 'STOCK_EXCEEDED':
                    alert("Chỉ còn đủ sản phẩm trong hệ thống, vui lòng kiểm tra lại số lượng");
                    break;
                case 'CONNECTION_ERROR':
                default:
                    alert("Không thể cập nhật giỏ hàng lúc này, vui lòng thử lại sau");
                    break;
            }
        });
}

function continueShopping() {
    const modal = document.getElementById('cart-modal');
    if (modal) {
        modal.style.display = 'none';
    }
    if (cartModalTimeout) clearTimeout(cartModalTimeout);
}

function viewCart() {
    if (cartModalTimeout) clearTimeout(cartModalTimeout);
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const cartUrl = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;
    window.location.href = cartUrl;
}

/**
 * Luồng phụ 6b: Xóa sản phẩm ra khỏi giỏ hàng qua Ajax Fetch
 */
function deleteItemAjax(productId) {
    // Bước 6b2: Hiển thị thông báo xác nhận xóa sản phẩm
    if (!confirm("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng không?")) {
        return; // E6b3: Hủy xác nhận, giữ nguyên trạng thái
    }

    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const baseUrl = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;

    const params = new URLSearchParams();
    params.append("action", "DELETE");
    params.append("productId", productId);

    fetch(baseUrl, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
        body: params.toString()
    })
        .then(res => {
            if (!res.ok) throw new Error('CONNECTION_ERROR');
            return res.json();
        })
        .then(data => {
            if (data.success) {
                // Bước 6b6: Hệ thống cập nhật hiển thị giao diện xóa dòng TR tương ứng
                const row = document.getElementById(`row-${productId}`);
                if (row) {
                    row.remove();
                }

                // Cập nhật lại chuỗi hiển thị tổng tiền hóa đơn mới
                document.getElementById("total-amount").innerText = new Intl.NumberFormat('vi-VN').format(data.newTotal);

                // Nếu xóa hết sạch dòng sản phẩm, tự động tải lại để hiện giao diện trống (Luồng E3a)
                const remainingRows = document.querySelectorAll(".cart-table tbody tr");
                if (remainingRows.length === 0) {
                    window.location.reload();
                }
            }
        })
        .catch(err => {
            alert("Không thể xóa sản phẩm lúc này do lỗi kết nối máy chủ.");
            console.error(err);
        });
}

function updateQuantityAjax(productId, inputElement) {
    const newQty = parseInt(inputElement.value);
    const originalQty = parseInt(inputElement.getAttribute("data-last-valid"));

    if (newQty <= 0) {
        deleteItemAjax(productId);
        return;
    }

    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const baseUrl = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;

    // Luồng phụ E6c1b: Gọi checkStock trước
    fetch(`${baseUrl}?queryType=checkStock&productId=${productId}&newQty=${newQty}`, {
        method: "POST"
    })
        .then(res => res.json())
        .then(data => {
            if (data.status === "outOfStock") {
                alert(`Hệ thống chỉ còn ${data.maxAvailable} sản phẩm tương thích trong kho.`);
                inputElement.value = originalQty; // Khôi phục lại số cũ hợp lệ
                return;
            }

            // Nếu kho đầy đủ, bắn lệnh UPDATE chính thức
            const params = new URLSearchParams();
            params.append("action", "UPDATE");
            params.append("productId", productId);
            params.append("newQty", newQty);

            fetch(baseUrl, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
                body: params.toString()
            })
                .then(res => res.json())
                .then(resData => {
                    if (resData.success) {
                        inputElement.setAttribute("data-last-valid", newQty); // Cập nhật bộ nhớ đệm
                        document.getElementById(`total-item-${productId}`).innerText = new Intl.NumberFormat('vi-VN').format(resData.itemTotal) + " VNĐ";
                        document.getElementById("total-amount").innerText = new Intl.NumberFormat('vi-VN').format(resData.cartTotal);
                    }
                });
        });
}

/**
 * Luồng phụ 6e: Tích chọn/Bỏ tích chọn sản phẩm
 */
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
        body: params.toString()
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                document.getElementById("total-amount").innerText = new Intl.NumberFormat('vi-VN').format(data.cartTotal);
            }
        });
}

/**
 * Luồng 6d: Người dùng nhấn nút "Đặt hàng ngay"
 * Kiểm tra xem người dùng đã tích chọn bất kỳ sản phẩm nào để tiến hành mua hay chưa
 */
function proceedToCheckout(event) {
    event.preventDefault();

    // Thu thập toàn bộ các ô checkbox đang được người dùng tích chọn mua
    const checkedBoxes = document.querySelectorAll(".item-checkbox:checked");

    if (checkedBoxes.length === 0) {
        alert("Vui lòng tích chọn ít nhất một sản phẩm trong giỏ hàng để tiến hành đặt hàng!");
        return;
    }

    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1));
    const baseUrl = (contextPath === "" || contextPath === "/") ? "/cart" : `${contextPath}/cart`;

    // Gửi yêu cầu kiểm tra trạng thái Session (Bước 6d1 -> 6d2)
    fetch(`${baseUrl}?queryType=checkAuth`, {
        method: "POST"
    })
        .then(res => {
            if (!res.ok) throw new Error('CONNECTION_ERROR');
            return res.json();
        })
        .then(data => {
            if (data.isGuest) {
                // BƯỚC 6d2: Nếu người dùng là Guest -> Yêu cầu đăng nhập trước
                alert("Bạn cần đăng nhập hệ thống trước khi tiến hành đặt hàng!");
                window.location.href = `${contextPath}/login`;
            } else {
                // BƯỚC 6d4: Nếu đã đăng nhập (User) -> Cho phép đi tiếp bước thu thập thông tin
                document.getElementById('cart-container').style.display = 'none';
                document.getElementById('order-info-container').style.display = 'block';
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        })
        .catch(err => {
            alert("Có lỗi xảy ra trong quá trình xác thực, vui lòng thử lại.");
            console.error(err);
        });
}