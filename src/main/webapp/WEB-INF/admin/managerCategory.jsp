<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <title>Category Manager</title>

    <!-- DataTable -->
    <link
      rel="stylesheet"
      href="https://cdn.datatables.net/1.13.8/css/jquery.dataTables.min.css"
    />
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
    <script src="https://cdn.datatables.net/1.13.8/js/jquery.dataTables.min.js"></script>

    <!-- Bootstrap -->
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
  </head>

  <body class="p-3">
    <h2>Quản lý danh mục</h2>

    <button class="btn btn-primary mb-3" onclick="openAddModal()">
      + Thêm danh mục
    </button>

    <table id="categoryTable" class="display" style="width: 100%">
      <thead>
        <tr>
          <th>ID</th>
          <th>Tên danh mục</th>
          <th>Trạng thái</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody></tbody>
    </table>

    <!-- MODAL ADD/EDIT -->
    <div class="modal fade" id="categoryModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="categoryModalTitle">Thêm danh mục</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
            ></button>
          </div>

          <div class="modal-body">
            <input type="hidden" id="categoryId" />

            <label>Tên danh mục</label>
            <input
              type="text"
              id="categoryName"
              class="form-control"
              required
            />
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">
              Huỷ
            </button>
            <button class="btn btn-success" onclick="saveCategory()">
              Lưu
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL CONFIRM TOGGLE -->
    <div class="modal fade" id="confirmModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Xác nhận</h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
            ></button>
          </div>

          <div class="modal-body">
            <p id="confirmMessage">
              Bạn có chắc chắn muốn thay đổi trạng thái danh mục này?
            </p>
            <input type="hidden" id="toggleCategoryId" />
          </div>

          <div class="modal-footer">
            <button class="btn btn-secondary" data-bs-dismiss="modal">
              Huỷ
            </button>
            <button class="btn btn-danger" onclick="executeToggle()">
              Đồng ý
            </button>
          </div>
        </div>
      </div>
    </div>

    <script>
      const API_URL = "http://localhost:8080/api/category";
      let table;
      const categoryModal = new bootstrap.Modal(
        document.getElementById("categoryModal"),
      );
      const confirmModal = new bootstrap.Modal(
        document.getElementById("confirmModal"),
      );

      $(document).ready(function () {
        loadData();
      });

      function loadData() {
        fetch(API_URL)
          .then((res) => res.json())
          .then((result) => {
            if (result.success) {
              initTable(result.data.categorys);
            } else {
              alert("Lỗi tải dữ liệu: " + result.message);
            }
          })
          .catch((err) => console.error(err));
      }

      function initTable(data) {
        if (table) {
          table.destroy();
        }
        table = $("#categoryTable").DataTable({
          data: data,
          columns: [
            { data: "id" },
            { data: "name" },
            {
              data: "active",
              render: function (data, type, row) {
                return data
                  ? '<span class="badge bg-success">Đang hoạt động</span>'
                  : '<span class="badge bg-secondary">Đã tắt</span>';
              },
            },
            {
              data: null,
              render: function (data, type, row) {
                let btnText = row.active ? "Tắt" : "Bật";
                let btnClass = row.active ? "btn-warning" : "btn-info";

                return `
                                    <button class="btn btn-sm btn-primary" onclick="openEditModal(\${row.id},'\${row.name}')">Sửa</button>
                                    <button class="btn btn-sm \${btnClass}" onclick="openToggleModal(\${row.id}, \${row.active})">\${btnText}</button>
                                `;
              },
            },
          ],
        });
      }

      function openAddModal() {
        document.getElementById("categoryModalTitle").innerText =
          "Thêm danh mục";
        document.getElementById("categoryId").value = "";
        document.getElementById("categoryName").value = "";
        categoryModal.show();
      }

      function openEditModal(id, name) {
        document.getElementById("categoryModalTitle").innerText =
          "Sửa danh mục";
        document.getElementById("categoryId").value = id;
        document.getElementById("categoryName").value = name;
        categoryModal.show();
      }

      function saveCategory() {
        const id = document.getElementById("categoryId").value;
        const name = document.getElementById("categoryName").value.trim();

        if (!name) {
          alert("Vui lòng nhập tên danh mục");
          return;
        }

        const method = id ? "PUT" : "POST";
        const body = { name: name };
        if (id) body.id = parseInt(id);

        fetch(API_URL, {
          method: method,
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(body),
        })
          .then((res) => res.json())
          .then((result) => {
            if (result.success) {
              categoryModal.hide();
              loadData();
            } else {
              alert(result.message);
            }
          })
          .catch((err) => console.error(err));
      }

      function openToggleModal(id, isActive) {
        document.getElementById("toggleCategoryId").value = id;
        const action = isActive ? "tắt" : "bật";
        document.getElementById("confirmMessage").innerText =
          `Bạn có chắc chắn muốn \${action} danh mục này?`;
        confirmModal.show();
      }

      function executeToggle() {
        const id = document.getElementById("toggleCategoryId").value;

        fetch(`\${API_URL}?id=\${id}`, {
          method: "DELETE",
        })
          .then((res) => res.json())
          .then((result) => {
            if (result.success) {
              confirmModal.hide();
              alert(result.message);
              loadData();
            } else {
              alert(result.message);
            }
          })
          .catch((err) => console.error(err));
      }
    </script>
  </body>
</html>
