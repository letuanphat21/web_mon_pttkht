function addToCart(productId) {
    // 1 & 2. Lấy số lượng từ input đã chọn
    const qtyInput = document.getElementById('qty-' + productId);
    const quantity = qtyInput ? parseInt(qtyInput.value) : 1;

    // BR1.27-2: Kiểm tra số lượng phải là số nguyên dương (>0)
    if (isNaN(quantity) || quantity <= 0) {
        alert("Số lượng thêm vào phải là số nguyên dương (>0)");
        return;
    }

    fetch('add-to-cart', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'productId=' + productId + '&quantity=' + quantity
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Kết nối thất bại');
            }
            return response.json();
        })
        .then(data => {
            if (data.status === 'success') {
                // NFR1.27-2. Cập nhật số lượng trên Header ngay lập tức
                const cartBadge = document.getElementById('cart-count');
                if (cartBadge) {
                    cartBadge.innerText = data.totalQty;
                }
                showCartModal();
            }
        })
        .catch(error => {
            // Exception Flow E. Lỗi kết nối
            alert("Không thể kết nối máy chủ, vui lòng thử lại.");
            console.error('Error:', error);
        });
}

function updateQuantityAjax(productId, quantity) {
    if (quantity <= 0) {
        deleteItemAjax(productId);
        return;
    }

    const params = new URLSearchParams();
    params.append('action', 'update');
    params.append('productId', productId);
    params.append('quantity', quantity);

    fetch('cart', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(response => {
            if (!response.ok) return response.text().then(text => { throw new Error(text) });
            return response.json();
        })
        .then(data => {
            if (data.status === 'success') {
                const itemTotalElem = document.getElementById('total-item-' + productId);
                if (itemTotalElem) {
                    itemTotalElem.innerText = data.itemTotal.toLocaleString('vi-VN') + " VNĐ";
                }

                const cartTotalElem = document.getElementById('total-amount');
                if (cartTotalElem) {
                    cartTotalElem.innerText = data.cartTotal.toLocaleString('vi-VN');
                }

                if (data.isEmpty) location.reload();
            }
        })
        .catch(error => {
            console.error('Lỗi:', error);
            alert("Có lỗi xảy ra khi cập nhật giỏ hàng.");
        });
}
function deleteItemAjax(productId) {
    if (!confirm("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")) {
        location.reload();
        return;
    }

    const params = new URLSearchParams();
    params.append('action', 'delete');
    params.append('productId', productId);

    fetch('cart', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                if (data.isEmpty) {
                    location.reload();
                    return;
                }

                const row = document.getElementById('row-' + productId);
                if (row) {
                    row.remove();
                }

                const cartTotalElem = document.getElementById('total-amount');
                if (cartTotalElem) {
                    cartTotalElem.innerText = data.cartTotal.toLocaleString('vi-VN');
                }

                const cartBadge = document.getElementById('cart-count');
                if (cartBadge && data.totalQty !== undefined) {
                    cartBadge.innerText = data.totalQty;
                }
            }
        })
        .catch(error => {
            console.error('Lỗi xóa:', error);
            alert("Không thể xóa sản phẩm.");
        });
}

function showCartModal() {
    const modal = document.getElementById('cart-modal');
    modal.style.display = 'block';

    // 6.3. Không làm gì (Tự động đóng sau 5 giây)
    // Clear timeout cũ nếu có để tránh xung đột khi nhấn liên tục
    if (window.cartTimeout) clearTimeout(window.cartTimeout);

    window.cartTimeout = setTimeout(() => {
        continueShopping();
    }, 5000);
}

function continueShopping() {
    const modal = document.getElementById('cart-modal');
    modal.style.display = 'none';
}

function viewCart() {
    window.location.href = 'cart';
}