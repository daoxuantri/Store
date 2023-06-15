package hcmute.edu.vn.store.utils;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.store.model.GioHang;
import hcmute.edu.vn.store.model.User;

public class Utils {
    public static final String BASE_URL="http://192.168.1.4/banhang/";

    public static List<GioHang> manggiohang;
    public static List<GioHang> mangmuahang = new ArrayList<>();
    public static User user_current = new User();
}
