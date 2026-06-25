package com.example.ltmb_nhom11.repository;

import com.example.ltmb_nhom11.model.Package;

import java.util.ArrayList;
import java.util.List;

public class PackageRepository {

    public List<Package> getPackages() {

        List<Package> packages = new ArrayList<>();

        packages.add(new Package(
                "PK01",
                "Gói khám tổng quát",
                "Khám sức khỏe cơ bản",
                500000
        ));

        packages.add(new Package(
                "PK02",
                "Gói tim mạch",
                "Khám chuyên sâu tim mạch",
                1200000
        ));

        packages.add(new Package(
                "PK03",
                "Gói VIP",
                "Khám toàn diện",
                2500000
        ));

        return packages;
    }

}