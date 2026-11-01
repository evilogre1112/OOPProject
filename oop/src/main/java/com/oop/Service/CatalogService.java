package com.oop.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.oop.DAO.AdjustDAO;
import com.oop.DAO.CatalogDAO;
import com.oop.EXCEPTION.PermissionException;
import com.oop.Model.Account;
import com.oop.Model.Adjust;
import com.oop.Model.Brand;
import com.oop.Model.Category;

public class CatalogService {

    // ===== Category =====

    /** Lấy tất cả danh mục. */
    public static List<Category> getAllCategories() throws SQLException {
        return  CatalogDAO.findAllCategories();
    }

    /** [Admin] Thêm danh mục + ghi nhật ký. */
    public static boolean addCategory(Category category,AccountService service) throws SQLException, PermissionException {
        Account user = service.getCurrentAccount();
        if(user == null || !user.getRole().equals("Admin")) throw new PermissionException();
        if (!CatalogDAO.insertCategory(category))return false;
        
        Adjust adjust = new Adjust();
        adjust.setAdminPhoneNum(user.getPhoneNum());
        adjust.setTypeAdjust("INSERT");
        adjust.setObjectNameAdjusted(category.getName());
        adjust.setTypeObject("Category");
        adjust.setIdObjectAdjusted(category.getId());

        return AdjustDAO.insert(adjust); // Phần này thảo luận sau về roll back -.+ quá oải
    }

    /** [Admin] Sửa danh mục + ghi nhật ký. */
    public static boolean updateCategory(Category category, String newName, AccountService service) throws SQLException,PermissionException {
        Account user = service.getCurrentAccount();
        if(user == null || !user.getRole().equals("Admin")) throw new PermissionException();
        Category category2 = new Category(category.getId(),newName);
        if(!CatalogDAO.updateCategory(category2)) return false ;
        Adjust adjust = new Adjust();
        adjust.setAdminPhoneNum(user.getPhoneNum());
        adjust.setTypeAdjust("CHANGENAME");
        adjust.setObjectNameAdjusted(category.getName());
        adjust.setTypeObject("Category");
        adjust.setIdObjectAdjusted(category.getId());
        return AdjustDAO.insert(adjust); // Phần này thảo luận sau về roll back -.+ quá oải
    }

    /** [Admin] Xóa danh mục (chặn nếu còn nhóm sản phẩm liên kết) + ghi nhật ký. */
    public static boolean deleteCategory(String categoryId, AccountService service) throws SQLException,PermissionException {
        Account user = service.getCurrentAccount();
        if(user == null || !user.getRole().equals("Admin")) throw new PermissionException();
        String Name = CatalogDAO.findCategoryById(categoryId).getName();
        if(!CatalogDAO.deleteCategory(categoryId)) return false ;
        Adjust adjust = new Adjust();
        adjust.setAdminPhoneNum(user.getPhoneNum());
        adjust.setTypeAdjust("DELETED");
        adjust.setObjectNameAdjusted(Name);
        adjust.setTypeObject("Category");
        adjust.setIdObjectAdjusted(categoryId);
        
        return AdjustDAO.insert(adjust);// Phần này thảo luận sau về roll back -.+ quá oải
    }

    // ===== Brand =====

    /** Lấy tất cả thương hiệu. */
    public static List<Brand> getAllBrands() throws SQLException {
         return  CatalogDAO.findAllBrands();
    }

    /** [Admin] Thêm thương hiệu (có logo) + ghi nhật ký. */
     public static boolean addBrand(Brand brand, AccountService service) throws SQLException, PermissionException {
        Account user = service.getCurrentAccount();
        if(user == null || !user.getRole().equals("Admin")) throw new PermissionException();

        brand.setId(UUID.randomUUID().toString());

        if(!CatalogDAO.insertBrand(brand)) return false;

        Adjust adjust = new Adjust();
        adjust.setAdminPhoneNum(user.getPhoneNum());
        adjust.setTypeAdjust("INSERT");
        adjust.setObjectNameAdjusted(brand.getName());
        adjust.setTypeObject("Brand");
        adjust.setIdObjectAdjusted(brand.getId());

        return AdjustDAO.insert(adjust); // Phần này thảo luận sau về roll back -.+ quá oải
    }

    /** [Admin] Sửa thương hiệu + ghi nhật ký. */
    public static boolean updateBrand(Brand brand, String newName, String newLogo, AccountService service) throws SQLException, PermissionException {
        Account user = service.getCurrentAccount();
        if(user == null || !user.getRole().equals("Admin")) throw new PermissionException();

        Brand brand2 = new Brand(brand.getId(), newName, newLogo);
        if(!CatalogDAO.updateBrand(brand2)) return false;

        Adjust adjust = new Adjust();
        adjust.setAdminPhoneNum(user.getPhoneNum());
        adjust.setTypeAdjust("UPDATE");
        adjust.setObjectNameAdjusted(brand.getName());
        adjust.setTypeObject("Brand");
        adjust.setIdObjectAdjusted(brand.getId());

        return AdjustDAO.insert(adjust); // Phần này thảo luận sau về roll back -.+ quá oải
    }

    /** [Admin] Xóa thương hiệu (chặn nếu còn nhóm sản phẩm liên kết) + ghi nhật ký. */
    public static boolean deleteBrand(String brandId, AccountService service) throws SQLException, PermissionException {
        Account user = service.getCurrentAccount();
        if(user == null || !user.getRole().equals("Admin")) throw new PermissionException();

        String Name = CatalogDAO.findBrandById(brandId).getName();
        if(!CatalogDAO.deleteBrand(brandId)) return false;

        Adjust adjust = new Adjust();
        adjust.setAdminPhoneNum(user.getPhoneNum());
        adjust.setTypeAdjust("DELETED");
        adjust.setObjectNameAdjusted(Name);
        adjust.setTypeObject("Brand");
        adjust.setIdObjectAdjusted(brandId);

        return AdjustDAO.insert(adjust); // Phần này thảo luận sau về roll back -.+ quá oải
    }
}
