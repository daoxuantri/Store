package hcmute.edu.vn.store.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.store.R;
import hcmute.edu.vn.store.adapter.DonHangAdapter;
import hcmute.edu.vn.store.model.DonHang;
import hcmute.edu.vn.store.model.DonHangModel;
import hcmute.edu.vn.store.model.EventBus.DonHangEvent;
import hcmute.edu.vn.store.model.NotiSendData;
import hcmute.edu.vn.store.retrofit.ApiBanHang;
import hcmute.edu.vn.store.retrofit.ApiPushNofication;
import hcmute.edu.vn.store.retrofit.RetrofitClient;
import hcmute.edu.vn.store.retrofit.RetrofitClientNoti;
import hcmute.edu.vn.store.utils.Utils;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class XemDonActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    ApiBanHang apiBanHang;
    RecyclerView redonhang;
    Toolbar toolbar;
    DonHang donHang;
    int tinhtrang;
    AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xem_don);
        initView();
        initToolbar();
        getOrder();}

    private void getOrder() {
//        apiBanHang= RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        compositeDisposable.add(apiBanHang.xemDonHang(0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        donHangModel -> {
                            DonHangAdapter adapter = new DonHangAdapter(getApplicationContext(), donHangModel.getResult());
                            redonhang.setAdapter(adapter);
                        },
                        throwable -> {


                        }
                ));
    }


    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        apiBanHang= RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        redonhang = findViewById(R.id.recycleview_donhang);
        toolbar = findViewById(R.id.toolbar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        redonhang.setLayoutManager(layoutManager);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    private void pushNotiToUser() {
        //gettoken, donHang.getIduser()
        compositeDisposable.add(apiBanHang.gettoken(0,donHang.getIduser())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(

                        userModel -> {
                            if(userModel.isSuccess()){
                                for(int i=0; i<userModel.getResult().size(); i++){
                                    Map<String, String> data = new HashMap<>();
                                    data.put("title","thongbao");
                                    data.put("body",trangThaiDon(tinhtrang));
                                    NotiSendData notiSendData = new NotiSendData(userModel.getResult().get(i).getToken(), data);
                                    ApiPushNofication apiPushNofication = RetrofitClientNoti.getInstance().create(ApiPushNofication.class);
                                    compositeDisposable.add(apiPushNofication.sendNofitication(notiSendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    notiRespone -> {

                                                    },
                                                    throwable -> {
                                                        Log.d("logg",throwable.getMessage());
                                                    }
                                            ));
                                }
                            }

                        },
                        throwable -> {
                            Log.d("logg", throwable.getMessage());
                        }
                ));

    }

    private String trangThaiDon(int status){
        String result ="";
        switch (status){
            case 0:
                result= "Đơn hàng đang được xử lí";
                break;
            case  1:
                result ="Đơn hàng đã được chấp nhận";
                break;
            case 2:
                result = "Đơn hàng đã giao cho đơn vị vận chuyển";
                break;
            case 3:
                result = "Thành công";
                break;
            case 4:
                result = "Đơn hàng đã hủy";
                break;

        }
        return result;
    }
    private void showCustomDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_donhang,null);
        Spinner spinner = view.findViewById(R.id.spinner_dialog);
        AppCompatButton btndongy= view.findViewById(R.id.dongy_dialog);
        List<String> list = new ArrayList<>();
        list.add("Đơn hàng đang được xử lí");
        list.add("Đơn hàng đã chấp nhận");
        list.add("Đơn hàng đã giao cho đơn vị vận chuyển");
        list.add("Thành công");
        list.add("Đơn hàng đã hủy");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,list);
        spinner.setAdapter(adapter);
        spinner.setSelection(donHang.getTrangthai());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tinhtrang=i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        btndongy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capNhapDonHang();
            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }


    private void capNhapDonHang() {
        compositeDisposable.add(apiBanHang.updateOrder(donHang.getId(),tinhtrang)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        messageModel -> {
                            getOrder();
                            dialog.dismiss();
                            pushNotiToUser();
                        },
                        throwable -> {

                        }
                ));
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void  evenDonHang(DonHangEvent event){
        if(event != null){
            donHang = event.getDonHang();
            showCustomDialog();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}