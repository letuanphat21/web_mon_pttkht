<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Tắt EL để JS template literal ${} không bị parse nhầm --%>
<%@ page isELIgnored="true" %>
<html>
<head>
  <meta charset="UTF-8">
  <title>Quản lý sản phẩm</title>

  <link rel="stylesheet" href="https://cdn.datatables.net/1.13.8/css/jquery.dataTables.min.css"/>
  <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
  <script src="https://cdn.datatables.net/1.13.8/js/jquery.dataTables.min.js"></script>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

  <style>
    .product-img-preview { width: 60px; height: 60px; object-fit: cover; border-radius: 4px; }
    .badge-active   { background-color: #198754; color:#fff; padding:4px 8px; border-radius:4px; }
    .badge-inactive { background-color: #6c757d; color:#fff; padding:4px 8px; border-radius:4px; }
  </style>
</head>
<body class="p-3">

<h2>Quản lý sản phẩm</h2>
<button class="btn btn-primary mb-3" onclick="openAddModal()">+ Thêm sản phẩm</button>

<table id="productTable" class="display" style="width:100%">
  <thead>
  <tr>
    <th>ID</th>
    <th>Ảnh</th>
    <th>Tên sản phẩm</th>
    <th>Thương hiệu</th>
    <th>Danh mục</th>
    <th>Giá (VNĐ)</th>
    <th>Tồn kho</th>
    <th>Trạng thái</th>
    <th>Hành động</th>
  </tr>
  </thead>
  <tbody></tbody>
</table>

<!-- ===== MODAL THÊM / SỬA ===== -->
<div class="modal fade" id="productModal" tabindex="-1">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="productModalTitle">Thêm sản phẩm</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <input type="hidden" id="productId"/>
        <div class="row g-3">
          <div class="col-md-6">
            <label class="form-label">Tên sản phẩm <span class="text-danger">*</span></label>
            <input type="text" id="productName" class="form-control" placeholder="Nhập tên sản phẩm"/>
          </div>
          <div class="col-md-6">
            <label class="form-label">Thương hiệu</label>
            <input type="text" id="productBrand" class="form-control" placeholder="Nike, Adidas,..."/>
          </div>
          <div class="col-md-6">
            <label class="form-label">Giá (VNĐ) <span class="text-danger">*</span></label>
            <input type="number" id="productPrice" class="form-control" min="1000" placeholder="VD: 250000"/>
          </div>
          <div class="col-md-6">
            <label class="form-label">Số lượng tồn kho <span class="text-danger">*</span></label>
            <input type="number" id="productQty" class="form-control" min="0" placeholder="VD: 50"/>
          </div>
          <div class="col-md-6">
            <label class="form-label">Danh mục <span class="text-danger">*</span></label>
            <select id="productCategoryId" class="form-select">
              <option value="">-- Chọn danh mục --</option>
            </select>
          </div>
          <div class="col-md-6">
            <label class="form-label">URL ảnh sản phẩm</label>
            <input type="text" id="productImage" class="form-control" placeholder="https://..."/>
          </div>
          <div class="col-12">
            <label class="form-label">Mô tả</label>
            <textarea id="productDesc" class="form-control" rows="3" placeholder="Mô tả ngắn về sản phẩm..."></textarea>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Huỷ</button>
        <button type="button" class="btn btn-success" onclick="saveProduct()">Lưu</button>
      </div>
    </div>
  </div>
</div>

<!-- ===== MODAL XÁC NHẬN TOGGLE ===== -->
<div class="modal fade" id="confirmModal" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">Xác nhận</h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
      </div>
      <div class="modal-body">
        <p id="confirmMessage">Bạn có chắc chắn muốn thay đổi trạng thái sản phẩm này?</p>
        <input type="hidden" id="toggleProductId"/>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Huỷ</button>
        <button type="button" class="btn btn-danger" onclick="executeToggle()">Đồng ý</button>
      </div>
    </div>
  </div>
</div>

<%-- API_URL dùng JSP expression (isELIgnored=true nên dùng <%= %>) --%>
<script>
  var API_URL = '<%= request.getContextPath() %>/api/product';
  var table;
  var productModal = new bootstrap.Modal(document.getElementById('productModal'));
  var confirmModal = new bootstrap.Modal(document.getElementById('confirmModal'));

  $(document).ready(function () {
    loadCategories();
    loadData();
  });

  // ===== LOAD DANH MỤC =====
  function loadCategories() {
    fetch(API_URL + '?type=categories')
            .then(function(res) { return res.json(); })
            .then(function(result) {
              if (!result.success) return;
              var sel = document.getElementById('productCategoryId');
              result.data.categories.forEach(function(c) {
                var opt = document.createElement('option');
                opt.value = c.id;
                opt.text  = c.name;
                sel.appendChild(opt);
              });
            });
  }

  // ===== LOAD BẢNG SẢN PHẨM =====
  function loadData() {
    fetch(API_URL)
            .then(function(res) { return res.json(); })
            .then(function(result) {
              if (result.success) {
                initTable(result.data.products);
              } else {
                alert('Lỗi tải dữ liệu: ' + result.message);
              }
            })
            .catch(function(err) { console.error(err); });
  }

  function initTable(data) {
    if (table) table.destroy();
    table = $('#productTable').DataTable({
      data: data,
      columns: [
        { data: 'productId' },
        {
          data: 'productImage',
          render: function(src) {
            var imgSrc = src || 'https://via.placeholder.com/60';
            return '<img src="' + imgSrc + '" class="product-img-preview" onerror="this.src=\'https://via.placeholder.com/60\'">';
          }
        },
        { data: 'productName' },
        { data: 'productBrand', defaultContent: '-' },
        { data: 'categoryName', defaultContent: '-' },
        {
          data: 'productPrice',
          render: function(val) {
            return Number(val).toLocaleString('vi-VN') + ' \u20ab';
          }
        },
        { data: 'quantity' },
        {
          data: 'productStatus',
          render: function(val) {
            return val === 1
                    ? '<span class="badge-active">Đang bán</span>'
                    : '<span class="badge-inactive">Đã khóa</span>';
          }
        },
        {
          data: null,
          render: function(data, type, row) {
            var btnToggle = row.productStatus === 1
                    ? '<button class="btn btn-sm btn-warning" onclick="openToggleModal(' + row.productId + ',' + row.productStatus + ')">Khóa</button>'
                    : '<button class="btn btn-sm btn-info"    onclick="openToggleModal(' + row.productId + ',' + row.productStatus + ')">Mở khóa</button>';

            return '<button class="btn btn-sm btn-primary me-1 btn-edit-product"'
                    + ' data-id="'    + row.productId + '"'
                    + ' data-name="'  + (row.productName        || '').replace(/"/g, '&quot;') + '"'
                    + ' data-brand="' + (row.productBrand       || '').replace(/"/g, '&quot;') + '"'
                    + ' data-price="' + row.productPrice + '"'
                    + ' data-qty="'   + row.quantity + '"'
                    + ' data-catid="' + row.categoryId + '"'
                    + ' data-img="'   + (row.productImage       || '').replace(/"/g, '&quot;') + '"'
                    + ' data-desc="'  + (row.productDescription || '').replace(/"/g, '&quot;') + '"'
                    + '>Sửa</button> ' + btnToggle;
          }
        }
      ]
    });

    // Event delegation cho nút Sửa (dùng data-* thay vì inline onclick)
    $(document).off('click', '.btn-edit-product').on('click', '.btn-edit-product', function() {
      var btn = $(this);
      openEditModal(
              btn.data('id'),
              btn.data('name'),
              btn.data('brand'),
              btn.data('price'),
              btn.data('qty'),
              btn.data('catid'),
              btn.data('img'),
              btn.data('desc')
      );
    });
  }

  // ===== MODAL THÊM =====
  function openAddModal() {
    document.getElementById('productModalTitle').innerText = 'Thêm sản phẩm';
    document.getElementById('productId').value           = '';
    document.getElementById('productName').value         = '';
    document.getElementById('productBrand').value        = '';
    document.getElementById('productPrice').value        = '';
    document.getElementById('productQty').value          = '';
    document.getElementById('productImage').value        = '';
    document.getElementById('productDesc').value         = '';
    document.getElementById('productCategoryId').value   = '';
    productModal.show();
  }

  // ===== MODAL SỬA =====
  function openEditModal(id, name, brand, price, qty, catId, img, desc) {
    document.getElementById('productModalTitle').innerText = 'Sửa sản phẩm';
    document.getElementById('productId').value           = id;
    document.getElementById('productName').value         = name;
    document.getElementById('productBrand').value        = brand;
    document.getElementById('productPrice').value        = price;
    document.getElementById('productQty').value          = qty;
    document.getElementById('productCategoryId').value   = catId;
    document.getElementById('productImage').value        = img;
    document.getElementById('productDesc').value         = desc;
    productModal.show();
  }

  // ===== LƯU (THÊM / SỬA) =====
  function saveProduct() {
    var id    = document.getElementById('productId').value;
    var name  = document.getElementById('productName').value.trim();
    var price = parseFloat(document.getElementById('productPrice').value);
    var qty   = parseInt(document.getElementById('productQty').value);
    var catId = parseInt(document.getElementById('productCategoryId').value);

    if (!name)               { alert('Vui lòng nhập tên sản phẩm'); return; }
    if (!price || price <= 0){ alert('Giá sản phẩm phải lớn hơn 0'); return; }
    if (isNaN(qty) || qty < 0){ alert('Số lượng không hợp lệ'); return; }
    if (!catId)              { alert('Vui lòng chọn danh mục'); return; }

    var body = {
      productName:        name,
      productBrand:       document.getElementById('productBrand').value.trim(),
      productPrice:       price,
      quantity:           qty,
      categoryId:         catId,
      productImage:       document.getElementById('productImage').value.trim(),
      productDescription: document.getElementById('productDesc').value.trim()
    };

    var method = id ? 'PUT' : 'POST';
    if (id) body.productId = parseInt(id);

    fetch(API_URL, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
            .then(function(res) { return res.json(); })
            .then(function(result) {
              if (result.success) {
                productModal.hide();
                loadData();
              } else {
                alert(result.message);
              }
            })
            .catch(function(err) { console.error(err); });
  }

  // ===== MODAL TOGGLE TRẠNG THÁI =====
  function openToggleModal(id, currentStatus) {
    document.getElementById('toggleProductId').value = id;
    var action = currentStatus === 1 ? 'khóa' : 'mở khóa';
    document.getElementById('confirmMessage').innerText = 'Bạn có chắc chắn muốn ' + action + ' sản phẩm này?';
    confirmModal.show();
  }

  function executeToggle() {
    var id = document.getElementById('toggleProductId').value;
    fetch(API_URL + '?id=' + id, { method: 'DELETE' })
            .then(function(res) { return res.json(); })
            .then(function(result) {
              if (result.success) {
                confirmModal.hide();
                alert(result.message);
                loadData();
              } else {
                alert(result.message);
              }
            })
            .catch(function(err) { console.error(err); });
  }
</script>

</body>
</html>
