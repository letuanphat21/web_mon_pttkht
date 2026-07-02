package org.example.webquanao.service;

import org.example.webquanao.action.Result;
import org.example.webquanao.dao.CategoryDAO;
import org.example.webquanao.dto.request.CategoryRequest;
import org.example.webquanao.dto.response.CategoryResponse;
import org.example.webquanao.entity.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryService {

    private CategoryDAO categoryDAO = new CategoryDAO();

    public Result getAllCategory() {
        Map<String, Object> data = new HashMap<>();

        try {
            List<Category> categorys = categoryDAO.findAll();
            List<CategoryResponse> responses = new ArrayList<>();
            for (Category c : categorys) {
                responses.add(new CategoryResponse(c.getId(), c.getName(), c.isActive()));
            }
            data.put("categorys", responses);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Lay danh sach that bai");
        }

        return Result.ok("Lay danh sach thanh cong", data);
    }

    public Result addCategory(CategoryRequest dto) {
        try {
            Category ct = categoryDAO.findByName(dto.getName());

            if (ct != null) {
                return Result.fail("Ten danh muc da ton tai");
            }

            Category category = new Category();
            category.setName(dto.getName());
            category.setActive(true);
            
            categoryDAO.insert(category);

            return Result.ok("Them danh muc thanh cong", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Loi he thong khi them danh muc");
        }
    }

    public Result updateCategory(CategoryRequest dto) {
        try {

            Category ct = categoryDAO.findByName(dto.getName());

            if (ct != null && ct.getId() != dto.getId()) {
                return Result.fail("Tên danh mục đã tồn tại");
            }
            Category existing = categoryDAO.findById(dto.getId());

            if (existing == null) {
                return Result.fail("Khong tim thay danh muc");
            }



            existing.setName(dto.getName());
            existing.setActive(dto.isActive());

            categoryDAO.update(existing);

            return Result.ok("Cap nhat danh muc thanh cong", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Cap nhat danh muc that bai");
        }
    }

    public Result toggleCategoryStatus(int id) {
        try {
            Category existing = categoryDAO.findById(id);
            if (existing != null) {
                if (existing.isActive()) {
                    categoryDAO.deactivate(id);
                } else {
                    categoryDAO.activate(id);
                }
                return Result.ok("Thay doi trang thai thanh cong", null);
            }
            return Result.fail("Khong tim thay danh muc");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("Thay doi trang thai that bai");
        }
    }

}
