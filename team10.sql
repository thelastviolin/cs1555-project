-- Kevin Ireland (kdi8), Zhiyao Wei (zhw28) first phase

CREATE TABLE ourSysDATE (
	c_date date,
	CONSTRAINT pk_date PRIMARY KEY (c_date)
);

CREATE TABLE Customer (
	login varchar2(10),
	password varchar2(10),
	name varchar2(20),
	address varchar2(30),
	email varchar2(20),
	CONSTRAINT pk_Customer PRIMARY KEY (login)
);

CREATE TABLE Administrator (
	login varchar2(10),
	password varchar2(10),
	name varchar2(20),
	address varchar2(30),
	email varchar2(20),
	CONSTRAINT pk_Admin PRIMARY KEY (login)
);

CREATE TABLE Product (
	auction_id int,
	name varchar2(20),
	description varchar2(30),
	seller varchar2(10),
	start_date date,
	min_price int,
	number_of_days int,
	status varchar2(15) NOT NULL,
	buyer varchar2(10),
	sell_date date,
	amount int,
	CONSTRAINT pk_Product PRIMARY KEY (auction_id),
	CONSTRAINT fk_Seller FOREIGN KEY (seller) REFERENCES Customer(login),
	CONSTRAINT fk_Buyer FOREIGN KEY (buyer) REFERENCES Customer(login)
);

CREATE TABLE Bidlog (
	bidsn int,
	auction_id int,
	bidder varchar2(10),
	bid_time date,
	amount int,
	CONSTRAINT pk_Bidlog PRIMARY KEY (bidsn),
	CONSTRAINT fk_BidToProduct FOREIGN KEY (auction_id) REFERENCES Product(auction_id),
	CONSTRAINT fk_Bidder FOREIGN KEY (bidder) REFERENCES Customer(login)
);

CREATE TABLE Category (
	name varchar2(20),
	parent_category varchar2(20),
	CONSTRAINT pk_Category PRIMARY KEY (name),
	CONSTRAINT fk_ParentCat FOREIGN KEY (parent_category) REFERENCES Category(name)	
);

CREATE TABLE BelongsTo (
	auction_id int,
	category varchar2(20),
	CONSTRAINT pk_BelongsTo PRIMARY KEY (auction_id, category),
	CONSTRAINT fk_BelongsId FOREIGN KEY (auction_id) REFERENCES Product(auction_id),
	CONSTRAINT fk_BelongsCat FOREIGN KEY (category) REFERENCES Category(name)
);

-- tri_bidTimeUpdate trigger
create or replace trigger tri_bidTimeUpdate
after insert
on Bidlog
begin
update ourSysDATE
set ourSysDATE.c_date = ourSysDATE.c_date + 5/86400;
end;
/

-- tri_updateHighBid trigger
create or replace trigger tri_updateHighBid
after insert
on Bidlog
begin
update Product
set Product.amount = :new.amount
where :new.auction_id = Product.auction_id;
end;
/

-- closeAuction trigger
create or replace trigger closeAuction
after update of c_date
on ourSysDATE
for each row
begin
update Product
set Product.status = 'close'
where Product.sell_date <= :new.c_date;
end;
/

-- Product_Count function: counts the number of products sold in the past x for specific category c
create or replace function Product_Count(cat in varchar2, month in integer) return integer is 
	num_products integer;
begin
	select count(auction_id) into num_products from (
		select auction_id
		from BelongsTo
		where cat = category) as a inner join Product on a.auction_id = Product.auction_id
	where Product.status = 'sold' and Product.sell_date <= add_months(sysdate, -month);
	return(num_products);
end;
/

-- Bid_Count function: count the number of bids of a user in the past x months
create or replace function Bid_Count(name in varchar2, month in integer) return integer is
	num_bids integer;
begin
	select count(bidsn) into num_bids
	from Bidlog
	where name = bidder and bid_time <= add_months(sysdate, -month);
	return(num_bids);
end;
/

-- Buying_Amount function: calculates the amount of money a particular user as spent in the past x months
create or replace function Buying_Amount(user in varchar2, month in integer) return integer is
	total_amount integer;
begin
	select sum(amount) into total_amount
	from Product
	where user = buyer and status = 'sold';
	return(total_amount);
end;
/

-- top k most active bidders in x months, currently replaced k with 2 (if k = 1) and x with 1 (during the last 1 month)
select bidder, Bid_Count(bidder, 1)
from Bidlog
where rownum < 2
group by bidder
order by Bid_Count(bidder, 1) desc;