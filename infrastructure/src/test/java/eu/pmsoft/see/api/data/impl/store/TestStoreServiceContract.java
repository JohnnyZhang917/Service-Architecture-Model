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

package eu.pmsoft.see.api.data.impl.store;

import eu.pmsoft.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;
import eu.pmsoft.see.api.data.architecture.contract.store.StoreOrder;
import eu.pmsoft.see.api.data.architecture.contract.store.StoreServiceContract;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TestStoreServiceContract implements StoreServiceContract {

    @Override
    public StoreOrder createNewOrder() {
        return new MyStoreOrder();
    }

    class MyStoreOrder implements StoreOrder {

        Map<String, Integer> order = new HashMap<String, Integer>();

        @Override
        public void addProduct(String productId, Integer nrOfProducts) {
            if (order.containsKey(productId)) {
                Integer current = order.get(productId);
                order.put(productId, current + nrOfProducts);
            } else {
                order.put(productId, nrOfProducts);
            }
        }

        @Override
        public boolean receiveExternalCourierService(CourierAddressSetupInfo adressSetup) {
            int currentSize = order.size();
            assert currentSize > 0 : "product must be already added during test execution";
            adressSetup.setCity("Warsaw");
            adressSetup.setStreet("Gorczewska");
            adressSetup.setPackageSize(order.size() + 1);
            boolean setupOk = adressSetup.setupDone();
            return setupOk;
        }

        @Override
        public boolean cancelOrder() {
            return false;
        }

        @Override
        public String realizeOrder() {
            StringBuffer buf = new StringBuffer();
            for (Entry<String, Integer> orderProduct : order.entrySet()) {
                buf.append("product:").append(orderProduct.getKey()).append("-").append(orderProduct.getValue()).append("\n");
            }
            return buf.toString();
        }

    }

}
