import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class SinhVien {
    String firstName;
    String lastName;
    String birthdate;
    String address;
    double oopScore;
    double projectManagementScore;
    double machineLearningScore;
    double databaseScore;
    double mobileDevScore;

    public SinhVien(String firstName, String lastName, String birthdate, String address, double oopScore, double projectManagementScore, double machineLearningScore, double databaseScore, double mobileDevScore) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthdate = birthdate;
        this.address = address;
        this.oopScore = oopScore;
        this.projectManagementScore = projectManagementScore;
        this.machineLearningScore = machineLearningScore;
        this.databaseScore = databaseScore;
        this.mobileDevScore = mobileDevScore;
    }

    public static SinhVien nhapSVTLU(Scanner sc) {
        System.out.println("Nhap ho va ten: ");
        String firstName = sc.nextLine();
        String lastName = sc.nextLine();
        System.out.println("Nhap ngay sinh: ");
        String birthdate = sc.nextLine();
        System.out.println("Nhap dia chi: ");
        String address = sc.nextLine();

        System.out.println("Nhap diem Lap trinh huong doi tuong: ");
        double oopScore = sc.nextDouble();
        System.out.println("Nhap diem Quan ly du an: ");
        double projectManagementScore = sc.nextDouble();
        System.out.println("Nhap diem Hoc may: ");
        double machineLearningScore = sc.nextDouble();
        System.out.println("Nhap diem Co so du lieu: ");
        double databaseScore = sc.nextDouble();
        System.out.println("Nhap diem Lap trinh ung dung cho TBDD: ");
        double mobileDevScore = sc.nextDouble();
        sc.nextLine(); // Clear buffer

        return new SinhVien(firstName, lastName, birthdate, address, oopScore, projectManagementScore, machineLearningScore, databaseScore, mobileDevScore);
    }

    public double tinhDiemTrungBinh() {
        return (oopScore + mobileDevScore + machineLearningScore + projectManagementScore + databaseScore) / 5;
    }

    public String xepLoaiSVTLU() {
        double xeploai = tinhDiemTrungBinh();
        if (xeploai >= 8.5) return "A";
        else if (xeploai >= 7) return "B";
        else if (xeploai >= 5.5) return "C";
        else if (xeploai >= 4) return "D";
        else return "F";
    }

    public void hienthiSVTLU() {
        System.out.println("Ho ten: " + firstName + " " + lastName);
        System.out.println("Dia chi: " + address);
        System.out.println("Sinh ngay: " + birthdate);
        System.out.println("Xep loai: " + xepLoaiSVTLU());
    }
}

class ClassTLU {
    String className;
    List<SinhVien> dssinhvien;

    public ClassTLU(String className) {
        this.className = className;
        this.dssinhvien = new ArrayList<>();
    }

    public void addSVTLU(SinhVien sv) {
        dssinhvien.add(sv);
    }

    public void hienthiDSSV() {
        for (SinhVien sv : dssinhvien) {
            sv.hienthiSVTLU();
        }
    }

    public void thongKeXL() {
        int DiemA = 0, DiemB = 0, DiemC = 0, DiemD = 0, DiemF = 0;
        for (SinhVien sv : dssinhvien) {
            switch (sv.xepLoaiSVTLU()) {
                case "A": DiemA++; break;
                case "B": DiemB++; break;
                case "C": DiemC++; break;
                case "D": DiemD++; break;
                case "F": DiemF++; break;
            }
        }
        System.out.println("Thong ke xep loai");
        System.out.println("A : " + DiemA);
        System.out.println("B : " + DiemB);
        System.out.println("C : " + DiemC);
        System.out.println("D : " + DiemD);
        System.out.println("F : " + DiemF);
    }
}

public class b1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<ClassTLU> dsLop = new ArrayList<>();

        System.out.println("Nhap so lop: ");
        int solop = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < solop; i++) {
            System.out.println("Nhap ten lop thu " + (i + 1) + ":");
            String className = sc.nextLine();
            ClassTLU lop = new ClassTLU(className);
            dsLop.add(lop);
        }

        System.out.println("Nhap so sinh vien: ");
        int soSinhVien = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < soSinhVien; i++) {
            System.out.println("Nhap thong tin sinh vien thu " + (i + 1) + ":");
            SinhVien sv = SinhVien.nhapSVTLU(sc);

            System.out.println("Chon lop cho sinh vien tu danh sach:");
            for (int j = 0; j < dsLop.size(); j++) {
                System.out.println((j + 1) + ". " + dsLop.get(j).className);
            }

            int chonLop;
            do {
                System.out.print("Nhap so thu tu lop: ");
                chonLop = sc.nextInt();
                sc.nextLine();
            } while (chonLop < 1 || chonLop > dsLop.size());

            ClassTLU lopDuocChon = dsLop.get(chonLop - 1);
            lopDuocChon.addSVTLU(sv);
        }

        for (ClassTLU lop : dsLop) {
            System.out.println("Danh sach sinh vien lop: " + lop.className);
            lop.hienthiDSSV();
            lop.thongKeXL();
        }
    }
}