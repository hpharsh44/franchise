create table "franchise"(
   id                  BIGSERIAL NOT NULL ,
                       PRIMARY KEY (id) ,
   name                VARCHAR(50) NOT NULL ,
   status              CHAR(50) NOT NULL ,
   created_at          TIMESTAMP NULL ,
   updated_at          TIMESTAMP NULL
);

insert into franchise (name, status, created_at, updated_at) values ( 'Starbucks', 'ACTIVE', now(), now());

create table "menu"(
   id                   BIGSERIAL NOT NULL ,
                        PRIMARY KEY (id) ,
   name                 VARCHAR(50) NOT NULL ,
   franchise             BIGSERIAL NOT NULL,
                        FOREIGN KEY (franchise)  REFERENCES franchise (id) ,
   start_time           VARCHAR(15) ,
   end_time             VARCHAR(15) ,
   status               CHAR(50) NOT NULL ,
   created_at           TIMESTAMP NULL ,
   updated_at           TIMESTAMP NULL
);

create table "menu_item"(
   id                   BIGSERIAL NOT NULL ,
                        PRIMARY KEY (id) ,
   menu                 BIGSERIAL NOT NULL ,
                        FOREIGN KEY (menu)  REFERENCES menu (id) ,
   item_name            VARCHAR(50) NOT NULL ,
   price                DECIMAL(15,2) NOT NULL ,
   quantity             INT NOT NULL ,
   created_at           TIMESTAMP NULL ,
   updated_at           TIMESTAMP NULL
);

create table "orders"(
    id                         BIGSERIAL NOT NULL ,
                               PRIMARY KEY (id) ,
    franchise                   BIGSERIAL NOT NULL ,
                               FOREIGN KEY (franchise)  REFERENCES franchise (id) ,
    menu_id                    BIGINT NOT NULL ,
    customer_id                BIGINT NOT NULL ,
    status                     CHAR(50) NOT NULL ,
    order_type                     CHAR(50) NOT NULL ,
    pick_up_time               TIMESTAMP,
    total                      DECIMAL(15,2) NOT NULL ,
    created_at                 TIMESTAMP  ,
    updated_at                 TIMESTAMP
 );

 create table "order_item"(
    id                   BIGSERIAL NOT NULL ,
                         PRIMARY KEY (id) ,
    menu_item_id         BIGINT NOT NULL,
    price                DECIMAL(15,2) NOT NULL ,
    quantity             INT NOT NULL ,
    orders       BIGSERIAL NOT NULL ,
                         FOREIGN KEY (orders)  REFERENCES orders (id) ,
    created_at           TIMESTAMP NULL ,
    updated_at           TIMESTAMP NULL
 );

insert into menu (name, franchise, start_time, end_time, status, created_at, updated_at)
values ('ColdDrinks', 1, '10:00 AM', '11:00 PM', 'ACTIVE', now(), now());


insert into menu_item (menu, item_name, price, quantity, created_at, updated_at)
values ( 1, 'Coffee', 200.00, 1, now(), now());

insert into menu_item (menu, item_name, price, quantity, created_at, updated_at)
values ( 1,  'Blueberry', 500.00, 1, now(), now());

insert into menu_item (menu, item_name, price, quantity, created_at, updated_at)
values ( 1, 'Lemon Coffee', 70.00, 1, now(), now());
