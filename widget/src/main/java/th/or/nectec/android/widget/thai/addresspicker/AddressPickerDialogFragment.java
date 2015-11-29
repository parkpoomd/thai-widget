/*
 * Copyright 2015 NECTEC
 * National Electronics and Computer Technology Center, Thailand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package th.or.nectec.android.widget.thai.addresspicker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import th.or.nectec.android.widget.thai.OnAddressChangedListener;
import th.or.nectec.android.widget.thai.R;
import th.or.nectec.android.widget.thai.addresspicker.adapter.DistrictAdapter;
import th.or.nectec.android.widget.thai.addresspicker.adapter.ProvinceAdapter;
import th.or.nectec.android.widget.thai.addresspicker.adapter.SubdistrictAdapter;
import th.or.nectec.android.widget.thai.addresspicker.handler.AddressPickerInterface;
import th.or.nectec.android.widget.thai.addresspicker.repository.EnumRegionRepository;
import th.or.nectec.android.widget.thai.addresspicker.repository.JsonDistrictRepository;
import th.or.nectec.android.widget.thai.addresspicker.repository.JsonProvinceRepository;
import th.or.nectec.android.widget.thai.addresspicker.repository.JsonSubdistrictRepository;
import th.or.nectec.domain.thai.address.district.DistrictChooser;
import th.or.nectec.domain.thai.address.district.DistrictPresenter;
import th.or.nectec.domain.thai.address.province.ProvinceChooser;
import th.or.nectec.domain.thai.address.province.ProvincePresenter;
import th.or.nectec.domain.thai.address.region.RegionChooser;
import th.or.nectec.domain.thai.address.region.RegionPresenter;
import th.or.nectec.domain.thai.address.subdistrict.SubdistrictChooser;
import th.or.nectec.domain.thai.address.subdistrict.SubdistrictListPresenter;
import th.or.nectec.entity.thai.Address;
import th.or.nectec.entity.thai.Province;
import th.or.nectec.entity.thai.Region;


public class AddressPickerDialogFragment extends DialogFragment implements AddressPickerInterface, AdapterView.OnItemClickListener {

    public static final String FRAGMENT_TAG = "address_dialog";


    private static final int SELECT_REGION = 0;
    private static final int SELECT_PROVINCE = 1;
    private static final int SELECT_DISTRICT = 2;
    private static final int SELECT_SUBDISTRICT = 3;
    OnAddressChangedListener addressChangedListener;
    ListView listView;


    Province provinceData;
    Address addressData;

    ArrayAdapter<String> regionAdapter;
    RegionChooser regionChooser;
    RegionPresenter regionPresenter = new RegionPresenter() {
        @Override
        public void showRegionList(List<Region> regions) {
            List<String> regionStringList = mapToListOfString(regions);
            regionAdapter = new ArrayAdapter<>(getActivity(), R.layout.address_picker_list_item, regionStringList);
        }

        @Override
        public void showNotFoundRegion() {
            Toast.makeText(getActivity(), "ไม่พบภูมิภาค", Toast.LENGTH_LONG).show();
        }

        public List<String> mapToListOfString(List<Region> regions) {
            List<String> stringList = new ArrayList<>();
            for (Region region : regions) {
                stringList.add(region.toString());
            }
            return stringList;
        }
    };
    DistrictAdapter districtAdapter;
    DistrictChooser districtChooser;
    DistrictPresenter districtPresenter = new DistrictPresenter() {
        @Override
        public void showDistrictList(List<Address> districts) {
            districtAdapter = new DistrictAdapter(getActivity(), districts);
        }

        @Override
        public void showNotFoundDistrict() {
            Toast.makeText(getActivity(), "ไม่พบอำเภอ", Toast.LENGTH_LONG).show();
        }
    };
    ProvinceAdapter provinceAdapter;
    ProvinceChooser provinceChooser;
    ProvincePresenter provincePresenter = new ProvincePresenter() {
        @Override
        public void showProvinceList(List<Province> provinces) {
            provinceAdapter = new ProvinceAdapter(getActivity(), provinces);
        }

        @Override
        public void showNotFoundProvince() {
            Toast.makeText(getActivity(), "ไม่พบจังหวัด", Toast.LENGTH_LONG).show();
        }
    };
    SubdistrictAdapter subdistrictAdapter;
    SubdistrictChooser subdistrictChooser;
    SubdistrictListPresenter subdistrictListPresenter = new SubdistrictListPresenter() {
        @Override
        public void showSubdistrictList(List<Address> districts) {
            subdistrictAdapter = new SubdistrictAdapter(getActivity(), districts);
        }

        @Override
        public void showNotFoundSubdistrict() {
            Toast.makeText(getActivity(), "ไม่พบตำบล", Toast.LENGTH_LONG).show();
        }
    };
    private int currentState = SELECT_REGION;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_address_list_picker, container, false);
        initInstances(view);

        return view;
    }

    private void initInstances(View view) {
        listView = (ListView) view.findViewById(R.id.picker_list);
        listView.setOnItemClickListener(this);

        bringToRegionList();
    }

    @Override
    public void bringToRegionList() {
        getDialog().setTitle(R.string.choose_region);
        regionChooser = new RegionChooser(new EnumRegionRepository(), regionPresenter);
        regionChooser.showRegionList();
        listView.setAdapter(regionAdapter);
        currentState = SELECT_REGION;
    }

    @Override
    public void bringToProvinceList(String region) {
        getDialog().setTitle(R.string.choose_province);
        provinceChooser = new ProvinceChooser(new JsonProvinceRepository(getActivity()), provincePresenter);
        provinceChooser.showProvinceListByRegion(Region.fromName(region));
        listView.setAdapter(provinceAdapter);
        currentState = SELECT_PROVINCE;
    }

    @Override
    public void bringToDistrictList(String provinceCode) {
        getDialog().setTitle(R.string.choose_district);
        districtChooser = new DistrictChooser(new JsonDistrictRepository(getActivity()), districtPresenter);
        districtChooser.showDistrictListByProvinceCode(provinceCode);
        listView.setAdapter(districtAdapter);
        currentState = SELECT_DISTRICT;
    }

    @Override
    public void bringToSubdistrictList(String districtCode) {
        getDialog().setTitle(R.string.choose_subdistrict);
        subdistrictChooser = new SubdistrictChooser(new JsonSubdistrictRepository(getActivity()), subdistrictListPresenter);
        subdistrictChooser.showSubdistrictListByDistrictCode(districtCode);
        listView.setAdapter(subdistrictAdapter);
        currentState = SELECT_SUBDISTRICT;
    }

    @Override
    public void bringAddressValueToAddressView(Address addressData) {
        if (addressChangedListener != null) {
            addressChangedListener.onAddressChanged(addressData);

        }
        dismiss();
    }

    public void setOnAddressChangedListener(OnAddressChangedListener addressChangedListener) {
        this.addressChangedListener = addressChangedListener;
    }

    @Override
    public void restoreAddressField(final Address address) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                addressData = address;
                bringToSubdistrictList(address.getAddressCode().substring(0, 4));
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (currentState == SELECT_REGION) {
            bringToProvinceList(regionAdapter.getItem(position));
        } else if (currentState == SELECT_PROVINCE) {
            provinceData = provinceAdapter.getItem(position);
            bringToDistrictList(provinceData.getCode());
        } else if (currentState == SELECT_DISTRICT) {
            addressData = districtAdapter.getItem(position);
            bringToSubdistrictList(addressData.getAddressCode());
        } else if (currentState == SELECT_SUBDISTRICT) {
            addressData = subdistrictAdapter.getItem(position);
            bringAddressValueToAddressView(addressData);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (currentState == SELECT_REGION) {
                    dismiss();
                } else if (currentState == SELECT_PROVINCE) {
                    bringToRegionList();
                } else if (currentState == SELECT_DISTRICT) {
                    bringToProvinceList(addressData.getRegion().toString());
                } else if (currentState == SELECT_SUBDISTRICT) {
                    bringToDistrictList(addressData.getAddressCode());
                }
            }
        };
    }
}