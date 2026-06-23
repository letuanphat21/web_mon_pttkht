<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<html>
<head>
  <meta charset="UTF-8">
  <title>Cửa hàng - Web Quần Áo</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
  <style>
    body { background: #fff; font-family: Arial, sans-serif; color: #333; }

    /* ===== SEARCH BAR ===== */
    .search-wrap {
      background: #f8f9fa;
      border-bottom: 1px solid #ddd;
      padding: 15px 0;
    }
    .search-wrap form { max-width: 1200px; margin: 0 auto; padding: 0 15px; display: flex; gap: 10px; }
    .search-wrap .input-group { flex: 1; }
    .search-wrap input { padding: 8px 12px; border: 1px solid #ccc; font-family: Arial, sans-serif; }
    .search-wrap button { padding: 8px 20px; font-family: Arial, sans-serif; }

    /* ===== LAYOUT ===== */
    .shop-layout { max-width: 1200px; margin: 20px auto; padding: 0 15px; display: flex; gap: 30px; }

    /* ===== SIDEBAR LỌC ===== */
    .filter-sidebar {
      width: 220px; flex-shrink: 0;
      background: #f9f9f9; border: 1px solid #eee;
      padding: 15px; border-radius: 6px;
      align-self: flex-start;
    }
    .filter-sidebar h5 { font-size: 16px; font-weight: bold; margin-bottom: 10px; color: #333; }
    .filter-sidebar hr { border-top: 1px solid #ddd; margin: 15px 0; }
    .filter-sidebar .form-check { margin-bottom: 6px; }
    .filter-sidebar .btn { width: 100%; margin-top: 5px; font-size: 13px; font-family: Arial, sans-serif; }

    /* ===== DANH SÁCH SẢN PHẨM ===== */
    .product-grid { flex: 1; }
    .grid-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; border-bottom: 2px solid #eee; padding-bottom: 10px; }
    .grid-header span { font-size: 14px; color: #666; }

    .products-wrap { display: flex; flex-wrap: wrap; gap: 20px; }

    /* ===== EMPTY STATE ===== */
    .empty-state { text-align: center; padding: 50px 0; color: #888; }
    .empty-state .icon { font-size: 40px; margin-bottom: 10px; color: #ccc; }
  </style>
</head>
<body>

<jsp:include page="header.jsp"/>

<div class="search-wrap">
  <form action="${pageContext.request.contextPath}/shop" method="get">
    <c:if test="${selectedCat > 0}">
      <input type="hidden" name="categoryId" value="${selectedCat}">
    </c:if>

    <div class="input-group">
      <input type="text" class="form-control" name="keyword" value="${keyword}"
             placeholder="Tìm kiếm sản phẩm, thương hiệu..."/>
      <button type="submit" class="btn btn-outline-primary"><i class="bi bi-search"></i> Tìm kiếm</button>
      <c:if test="${not empty keyword}">
        <a href="${pageContext.request.contextPath}/shop" class="btn btn-outline-secondary">
          <i class="bi bi-x-circle"></i>
        </a>
      </c:if>
    </div>
  </form>
</div>

<div class="shop-layout">

  <aside class="filter-sidebar">
    <form action="${pageContext.request.contextPath}/shop" method="get" id="filterForm">
      <c:if test="${not empty keyword}">
        <input type="hidden" name="keyword" value="${keyword}">
      </c:if>

      <h5><i class="bi bi-grid-fill me-1"></i> Danh mục</h5>
      <div class="form-check">
        <input class="form-check-input" type="radio" name="categoryId"
               value="0" id="cat-all"
               onchange="document.getElementById('filterForm').submit()"
        ${selectedCat == 0 ? 'checked' : ''}>
        <label class="form-check-label" for="cat-all">Tất cả</label>
      </div>
      <c:forEach var="cat" items="${categories}">
        <div class="form-check">
          <input class="form-check-input" type="radio" name="categoryId"
                 value="${cat.id}" id="cat-${cat.id}"
                 onchange="document.getElementById('filterForm').submit()"
            ${selectedCat == cat.id ? 'checked' : ''}>
          <label class="form-check-label" for="cat-${cat.id}">${cat.name}</label>
        </div>
      </c:forEach>

      <hr>
      <h5><i class="bi bi-wallet2 me-1"></i> Khoảng giá</h5>
      <input type="range" class="form-range" id="priceSlider"
             min="0" max="${maxPriceInDb}" step="10000"
             value="${selectedMax}"
             oninput="document.getElementById('maxDisplay').textContent = Number(this.value).toLocaleString('vi-VN')">
      <div style="font-size:13px; color:#555; margin-top:4px;">
        Đến: <strong id="maxDisplay"><fmt:formatNumber value="${selectedMax}" pattern="#,###"/></strong> ₫
      </div>
      <input type="hidden" name="minPrice" value="0">
      <input type="hidden" name="maxPrice" id="maxPriceInput" value="${selectedMax}">

      <button type="submit" class="btn btn-primary btn-sm mt-2"
              onclick="document.getElementById('maxPriceInput').value = document.getElementById('priceSlider').value">
        <i class="bi bi-funnel"></i> Áp dụng
      </button>
      <a href="${pageContext.request.contextPath}/shop" class="btn btn-outline-secondary btn-sm mt-1">
        <i class="bi bi-arrow-counterclockwise"></i> Đặt lại
      </a>
    </form>
  </aside>

  <div class="product-grid">
    <div class="grid-header">
      <span>
        <c:choose>
          <c:when test="${not empty keyword}">
            Kết quả tìm kiếm cho "<strong>${keyword}</strong>":
          </c:when>
          <c:otherwise>Tất cả sản phẩm</c:otherwise>
        </c:choose>
        &nbsp;<strong>${products.size()}</strong> sản phẩm
      </span>
    </div>

    <c:choose>
      <c:when test="${empty products}">
        <div class="empty-state">
          <div class="icon"><i class="bi bi-bag-x"></i></div>
          <p>Không tìm thấy sản phẩm nào phù hợp.</p>
          <a href="${pageContext.request.contextPath}/shop" class="btn btn-outline-primary btn-sm mt-2">Xem tất cả</a>
        </div>
      </c:when>
      <c:otherwise>
        <div class="products-wrap">
          <c:forEach var="p" items="${products}">
            <c:set var="safeDesc">
              <c:choose>
                <c:when test="${not empty p.productDescription}">${p.productDescription}</c:when>
                <c:otherwise>Chưa có mô tả cho sản phẩm này.</c:otherwise>
              </c:choose>
            </c:set>

            <%-- Khối thẻ sản phẩm bọc dữ liệu, thiết kế chuẩn mộc mạc, có xử lý chống lỗi ảnh ảo via.placeholder --%>
            <div class="product-card"
                 onclick="openProductModalFromData(this)"
                 data-id="${p.productId}"
                 data-name="<c:out value='${p.productName}' />"
                 data-price="${p.productPrice}"
                 data-image="${not empty p.productImage ? p.productImage : 'https://via.placeholder.com/200'}"
                 data-desc="${safeDesc}"
                 style="border: 1px solid #ddd; padding: 15px; width: 200px; border-radius: 8px; cursor: pointer; transition: transform 0.2s; background: #fff;">

              <img src="${not empty p.productImage ? p.productImage : 'https://via.placeholder.com/200'}"
                   alt="${p.productName}"
                   style="width: 100%; height: 200px; object-fit: cover; border-radius: 4px;"
                   onerror="this.src='https://via.placeholder.com/200'">

              <h4 style="margin: 10px 0 5px 0; color: #007bff; font-size: 18px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                  ${p.productName}
              </h4>

              <div style="font-size: 12px; color: #888; margin-bottom: 5px;">
                <i class="bi bi-tag"></i> ${not empty p.productBrand ? p.productBrand : 'Local Brand'}
              </div>

              <p style="color: #e44d26; font-weight: bold; font-size: 15px; margin-bottom: 0;">
                Giá: <fmt:formatNumber value="${p.productPrice}" pattern="#,###"/> ₫
              </p>

              <div style="text-align: center; margin-top: 10px;">
                <span style="font-size: 13px; color: white; background: #007bff; padding: 5px 12px; border-radius: 4px; display: inline-block;">
                  <i class="bi bi-eye"></i> Xem chi tiết
                </span>
              </div>
            </div>
          </c:forEach>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<div id="product-detail-modal" style="display:none; position:fixed; z-index:998; background:rgba(0,0,0,0.5); top:0; left:0; width:100%; height:100%;" onclick="if(event.target===this) closeProductModal()">
  <div style="background:white; width: 450px; padding:25px; position:absolute; top:50%; left:50%; transform:translate(-50%, -50%); border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.3); text-align: center;">

    <span onclick="closeProductModal()" style="position: absolute; right: 15px; top: 10px; font-size: 24px; cursor: pointer; color: #aaa;"><i class="bi bi-x-lg"></i></span>

    <img id="modal-p-image" src="" style="width: 100%; height: 250px; object-fit: cover; border-radius: 6px;" onerror="this.src='https://via.placeholder.com/200'">
    <h3 id="modal-p-name" style="margin: 15px 0 5px 0; color: #333; font-size: 22px;"></h3>
    <p id="modal-p-price" style="color: #e44d26; font-weight: bold; font-size: 20px; margin: 5px 0;"></p>

    <p style="font-weight: bold; margin-bottom: 2px; text-align: left;"><i class="bi bi-file-text"></i> Mô tả:</p>
    <p id="modal-p-desc" style="color: #666; font-size: 14px; text-align: left; margin-top: 0; background: #f9f9f9; padding: 10px; border-radius: 4px; max-height: 80px; overflow-y: auto;"></p>

    <hr style="border:0; border-top: 1px solid #eee; margin: 15px 0;">

    <div style="display: flex; align-items: center; justify-content: center; gap: 15px;">
      <label for="modal-p-qty" style="font-weight: 500;">Số lượng:</label>
      <input type="number" id="modal-p-qty" value="1" min="1" style="width: 60px; padding: 6px; text-align: center; border: 1px solid #ccc; border-radius: 4px;">

      <input type="hidden" id="modal-p-id">

      <button onclick="triggerAddToCartFromModal()" style="padding: 8px 20px; cursor: pointer; background: #28a745; color: white; border: none; border-radius: 5px; font-weight: bold;">
        <i class="bi bi-cart-plus"></i> Thêm vào giỏ
      </button>
    </div>
  </div>
</div>

<div id="cart-modal" style="display:none; position:fixed; z-index:999; background:white; border:1px solid #ccc; padding:20px; top:50%; left:50%; transform:translate(-50%, -50%); box-shadow: 0 4px 12px rgba(0,0,0,0.3); border-radius: 10px; min-width: 300px; text-align: center;">
  <h4 style="color: green; margin-top: 0; font-size: 18px;"><i class="bi bi-check-circle-fill"></i> Thêm sản phẩm thành công!</h4>
  <p style="color: #555;">Bạn muốn làm gì tiếp theo?</p>
  <hr style="border: 0; border-top: 1px solid #eee;">
  <div style="display: flex; justify-content: space-around; margin-top: 15px;">
    <button onclick="continueShopping()" style="padding: 8px 15px; cursor: pointer; background: #eee; border: 1px solid #ccc; border-radius: 4px;">Tiếp tục mua sắm</button>
    <button onclick="viewCart()" style="padding: 8px 15px; cursor: pointer; background: #007bff; color: white; border: none; border-radius: 4px; font-weight: bold;">Xem giỏ hàng</button>
  </div>
</div>

<div id="toast" style="display:none;position:fixed;bottom:28px;right:28px;
     background:#28a745;color:#fff;padding:12px 22px;border-radius:8px;
     font-size:15px;z-index:9999;box-shadow:0 4px 12px rgba(0,0,0,.2);">
  <i class="bi bi-check-circle"></i> Đã thêm vào giỏ hàng!
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/cart.js"></script>
<script>
  /* ===== HÀM ĐỌC DATASET TRÁNH LỖI URL CÓ DẤU / ===== */
  function openProductModalFromData(card) {
    const id    = card.getAttribute('data-id');
    const name  = card.getAttribute('data-name');
    const price = card.getAttribute('data-price');
    const image = card.getAttribute('data-image');
    const desc  = card.getAttribute('data-desc');

    document.getElementById('modal-p-id').value = id;
    document.getElementById('modal-p-name').textContent = name;
    document.getElementById('modal-p-price').textContent = Number(price).toLocaleString('vi-VN') + ' ₫';
    document.getElementById('modal-p-image').src = image || 'https://via.placeholder.com/200';
    document.getElementById('modal-p-desc').textContent = desc;
    document.getElementById('modal-p-qty').value = 1;

    document.getElementById('product-detail-modal').style.display = 'block';
  }

  function closeProductModal() {
    document.getElementById('product-detail-modal').style.display = 'none';
  }

  function triggerAddToCartFromModal() {
    const id = document.getElementById('modal-p-id').value;
    const qty = document.getElementById('modal-p-qty').value;

    postToCart(id, qty);
    closeProductModal();
  }

  function postToCart(productId, qty) {
    if (typeof executeAddToCartAJAX === 'function') {
      executeAddToCartAJAX(productId, qty);
    }
  }
</script>
</body>
</html>