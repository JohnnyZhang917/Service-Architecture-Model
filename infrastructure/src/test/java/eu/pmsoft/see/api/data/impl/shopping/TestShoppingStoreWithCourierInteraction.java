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

package eu.pmsoft.see.api.data.impl.shopping;

import com.google.inject.Inject;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierAddressSetupInfo;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceContract;
import eu.pmsoft.see.api.data.architecture.contract.courier.CourierServiceOrder;
import eu.pmsoft.see.api.data.architecture.contract.shopping.ShoppingStoreWithCourierInteraction;
import eu.pmsoft.see.api.data.architecture.contract.store.ProductOnOrder;
import eu.pmsoft.see.api.data.architecture.contract.store.StoreOrder;
import eu.pmsoft.see.api.data.architecture.contract.store.StoreServiceContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestShoppingStoreWithCourierInteraction implements ShoppingStoreWithCourierInteraction {

    private final CourierServiceContract courier;
    private final StoreServiceContract store;

    private Logger logger = LoggerFactory.getLogger(TestShoppingStoreWithCourierInteraction.class);

    @Inject
    public TestShoppingStoreWithCourierInteraction(CourierServiceContract courier, StoreServiceContract store) {
        super();
        this.courier = courier;
        this.store = store;
    }


    @Override
    public Integer makeShoping() {
        logger.debug("init shopping interaction");
        StoreOrder order = store.createNewOrder();
        order.addProduct(new ProductOnOrder("a", 1));
        order.addProduct(new ProductOnOrder("b", 1));

        CourierServiceOrder contract = courier.openOrder("myContractId");
        CourierAddressSetupInfo address = contract.setupAddress();
        boolean addressSetupOk = order.receiveExternalCourierService(address);
        if (!addressSetupOk) {
            throw new RuntimeException("address setup interaction failed");
        }
        Integer price = contract.getServicePrice();
        logger.debug("order price is {}", price);
        String orderDone = order.realizeOrder();
        logger.debug("My order contains:{}", orderDone);
        logger.debug("finish shopping interaction");
        return price;
    }

}
