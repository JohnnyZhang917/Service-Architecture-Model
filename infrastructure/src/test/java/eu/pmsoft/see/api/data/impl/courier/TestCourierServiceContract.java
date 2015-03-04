/*
 * Copyright (c) 2015. Paweł Cesar Sanjuan Szklarz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.pmsoft.see.api.data.impl.courier;

import com.google.common.base.Preconditions;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceContract;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceOrder;

public class TestCourierServiceContract implements CourierServiceContract {

    @Override
    public CourierServiceOrder openOrder(String contractId) {
        return new MyCourierServiceOrder();
    }

    class MyCourierServiceOrder implements CourierServiceOrder {
        String contractId;
        Integer price = 10000;
        MyCourierAddressSetupInfo info;

        @Override
        public CourierAddressSetupInfo setupAddress() {
            info = new MyCourierAddressSetupInfo();
            return info;
        }

        @Override
        public Integer getServicePrice() {
            Preconditions.checkState(info.setupDone);
            return price;
        }

        class MyCourierAddressSetupInfo implements CourierAddressSetupInfo {

            boolean setupDone = false;
            int countcall = 0;

            @Override
            public void setCity(String cityName) {
                price = price - 100;
                countcall++;
            }

            @Override
            public void setStreet(String streetName) {
                price = price - 100;
                countcall++;
            }

            @Override
            public void setPackageSize(Integer kgSize) {
                price = price - 1000;
                countcall++;
            }

            @Override
            public boolean setupDone() {
                if (countcall > 2) {
                    setupDone = true;
                }
                return setupDone;
            }

        }
    }

}
