package org.example.webquanao.service;

import org.example.webquanao.action.Result;
import org.example.webquanao.dao.CategoryDAO;
import org.example.webquanao.entity.Category;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryService {

    private CategoryDAO categoryDAO = new CategoryDAO();


    public Result getAllCategory() {
        Map<String,Object> data = new HashMap<>();

        try{
            List<Category> categorys = categoryDAO.findAll();
            data.put("categorys",categorys);
        }catch (Exception e){
            e.printStackTrace();
            return Result.fail("Lấy danh sách thất bại");
        }

        return Result.ok("Lấy danh sách thành công ", data);
    }

    public Result addCategory(Category category) {
        // try {

            Category ct = categoryDAO.findByName(category.getName());

            if (ct != null) {
                return Result.fail("Tên danh mục đã tồn tại");
            }

            category.setActive(true);
            categoryDAO.insert(category);

            return Result.ok("Thêm danh mục thành công", null);

        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return Result.fail("Lỗi hệ thống khi thêm danh mục");
        // }
    }

    public Result updateCategory(Category category) {
        // try {

            Category existing = categoryDAO.findByName(category.getName());

            if (existing != null) {
                return Result.fail("Tên danh mục đã tồn tại");
            }

            existing.setName(category.getName());
            existing.setActive(category.isActive());

            categoryDAO.update(existing);

            return Result.ok("Cập nhật danh mục thành công", null);

        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return Result.fail("Cập nhật danh mục thất bại");
        // }
    }

    public Result toggleCategoryStatus(int id) {
        // try {
             Category existing = categoryDAO.findById(id);
            if (existing != null) {
                if (existing.isActive()) {
                    categoryDAO.deactivate(id);
                } else {
                    categoryDAO.activate(id);
                }
                return Result.ok("Thay đổi trạng thái thành công", null);
            }
            return Result.fail("Không tìm thấy danh mục");
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return Result.fail("Thay đổi trạng thái thất bại");
        // }
    }

}
