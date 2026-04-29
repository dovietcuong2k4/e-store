CREATE DATABASE  IF NOT EXISTS `mydb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `mydb`;
-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: mydb
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `brands`
--

DROP TABLE IF EXISTS `brands`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brands` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brands`
--

LOCK TABLES `brands` WRITE;
/*!40000 ALTER TABLE `brands` DISABLE KEYS */;
INSERT INTO `brands` VALUES (2,'Apple',NULL),(3,'Asus',NULL),(4,'Acer',NULL),(5,'Dell',NULL),(6,'HP',NULL),(7,'Lenovo',NULL),(8,'MSI',NULL),(9,'Masstel',NULL),(10,'Haier',NULL),(11,'LG',NULL),(12,'Samsung',NULL),(13,'Logitech',NULL);
/*!40000 ALTER TABLE `brands` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cart_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `quantity` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cart_item_cart` (`cart_id`),
  KEY `fk_cart_item_product` (`product_id`),
  CONSTRAINT `fk_cart_item_cart` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`),
  CONSTRAINT `fk_cart_item_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_items`
--

LOCK TABLES `cart_items` WRITE;
/*!40000 ALTER TABLE `cart_items` DISABLE KEYS */;
INSERT INTO `cart_items` VALUES (1,1,35,1),(8,3,37,1),(9,3,33,1),(10,3,22,1);
/*!40000 ALTER TABLE `cart_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `carts`
--

DROP TABLE IF EXISTS `carts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `total_price` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cart_user` (`user_id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `carts`
--

LOCK TABLES `carts` WRITE;
/*!40000 ALTER TABLE `carts` DISABLE KEYS */;
INSERT INTO `carts` VALUES (1,5,890000),(2,1,0),(3,2,21670000),(4,3,0);
/*!40000 ALTER TABLE `carts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Laptop'),(3,'Thiết bị âm thanh & Giải trí'),(4,'Linh kiện máy tính'),(5,'Thiết bị lưu trữ'),(6,'Thiết bị mạng'),(7,'Camera giám sát'),(8,'Phụ kiện'),(9,'Thiết bị văn phòng'),(10,'PC & Máy tính bàn'),(11,'Màn hình'),(12,'Bàn phím & Chuột');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `contacts`
--

DROP TABLE IF EXISTS `contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `contacts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `reply_message` varchar(255) DEFAULT NULL,
  `contact_date` datetime DEFAULT NULL,
  `reply_date` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `responder_id` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_contact_responder` (`responder_id`),
  CONSTRAINT `fk_contact_responder` FOREIGN KEY (`responder_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `contacts`
--

LOCK TABLES `contacts` WRITE;
/*!40000 ALTER TABLE `contacts` DISABLE KEYS */;
INSERT INTO `contacts` VALUES (1,'customer@gmail.com','I need support',NULL,'2024-01-10 09:00:00',NULL,'NEW',NULL,NULL,NULL,NULL),(2,'dovietcuong2k4@gmail.com','1',NULL,'2026-04-16 08:46:39',NULL,'NEW',NULL,'Cường Đỗ Việt',NULL,'1');
/*!40000 ALTER TABLE `contacts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_history`
--

DROP TABLE IF EXISTS `order_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `from_status` varchar(255) DEFAULT NULL,
  `to_status` varchar(255) DEFAULT NULL,
  `changed_by` varchar(255) DEFAULT NULL,
  `changed_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_history_order` (`order_id`),
  CONSTRAINT `fk_order_history_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_history`
--

LOCK TABLES `order_history` WRITE;
/*!40000 ALTER TABLE `order_history` DISABLE KEYS */;
INSERT INTO `order_history` VALUES (1,3,NULL,'CREATED','ROLE_CUSTOMER (2)','2026-04-18 08:24:22'),(2,3,'CREATED','CANCELLED','ROLE_CUSTOMER (2)','2026-04-18 08:24:34'),(3,4,NULL,'CREATED','ROLE_CUSTOMER (2)','2026-04-18 08:24:58'),(4,4,'CREATED','PROCESSING','ROLE_STAFF (5)','2026-04-18 08:25:12'),(5,4,'PROCESSING','READY_FOR_SHIPPING','ROLE_STAFF (5)','2026-04-18 08:25:14'),(6,4,'READY_FOR_SHIPPING','SHIPPING','ROLE_SHIPPER (3)','2026-04-18 08:25:23'),(7,4,'SHIPPING','DELIVERED','ROLE_SHIPPER (3)','2026-04-18 08:25:25'),(8,5,NULL,'CREATED','ROLE_CUSTOMER (2)','2026-04-23 15:34:34'),(9,5,'CREATED','PROCESSING','ROLE_ADMIN (1)','2026-04-29 11:16:18'),(10,5,'PROCESSING','READY_FOR_SHIPPING','ROLE_ADMIN (1)','2026-04-29 11:16:36');
/*!40000 ALTER TABLE `order_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `price` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_item_order` (`order_id`),
  KEY `fk_order_item_product` (`product_id`),
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_order_item_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,37,1,690000),(2,2,37,1,690000),(3,3,35,1,890000),(4,4,35,1,890000),(5,5,35,1,890000),(6,5,37,1,690000),(7,5,33,1,5990000);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `shipper_id` bigint DEFAULT NULL,
  `receiver_name` varchar(255) DEFAULT NULL,
  `receiver_phone` varchar(255) DEFAULT NULL,
  `receiver_address` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `order_date` datetime DEFAULT NULL,
  `shipping_date` datetime DEFAULT NULL,
  `received_date` datetime DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `user_voucher_id` bigint DEFAULT NULL,
  `discount_amount` bigint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_order_user` (`user_id`),
  KEY `fk_order_shipper` (`shipper_id`),
  KEY `fk_order_user_voucher` (`user_voucher_id`),
  CONSTRAINT `fk_order_shipper` FOREIGN KEY (`shipper_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_order_user_voucher` FOREIGN KEY (`user_voucher_id`) REFERENCES `user_vouchers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,NULL,NULL,'Test1','1','1','','2026-04-17 11:17:21',NULL,NULL,'CREATED',NULL,0),(2,NULL,NULL,'1','1','1','','2026-04-17 11:23:52',NULL,NULL,'CREATED',NULL,0),(3,2,NULL,'Đỗ Việt Cường','1','1','','2026-04-18 08:24:22',NULL,NULL,'CANCELLED',NULL,0),(4,2,3,'Đỗ Việt Cường','1','1','','2026-04-18 08:24:58','2026-04-18 08:25:23','2026-04-18 08:25:25','DELIVERED',NULL,0),(5,2,NULL,'Đỗ Việt Cường','012','abc','','2026-04-23 15:34:34',NULL,NULL,'READY_FOR_SHIPPING',NULL,0);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_images`
--

DROP TABLE IF EXISTS `product_images`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `is_thumbnail` tinyint(1) DEFAULT '0',
  `sort_order` int DEFAULT '0',
  `public_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_product_images_product` (`product_id`),
  CONSTRAINT `fk_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=182 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_images`
--

LOCK TABLES `product_images` WRITE;
/*!40000 ALTER TABLE `product_images` DISABLE KEYS */;
INSERT INTO `product_images` VALUES (52,8,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149755/products/ipekr1svgs3hzu9sx2di.jpg',1,0,'products/ipekr1svgs3hzu9sx2di'),(53,8,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149755/products/zvgb7xl5vcx1yn2wjiug.jpg',0,1,'products/zvgb7xl5vcx1yn2wjiug'),(54,8,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149756/products/kgqogkunncukryeula9m.jpg',0,2,'products/kgqogkunncukryeula9m'),(55,9,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149758/products/tvodajd9ogh156snc9tq.jpg',1,0,'products/tvodajd9ogh156snc9tq'),(56,9,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149760/products/i3pwaqvwsokptqmbjbis.jpg',0,1,'products/i3pwaqvwsokptqmbjbis'),(57,9,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149761/products/wpunxv402ywch1slszfv.jpg',0,2,'products/wpunxv402ywch1slszfv'),(58,10,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149763/products/lqsijbwr2v9cah2lhduw.jpg',1,0,'products/lqsijbwr2v9cah2lhduw'),(59,10,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149764/products/g8t2bqwtiumopziejnia.jpg',0,1,'products/g8t2bqwtiumopziejnia'),(60,10,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149764/products/c4m1qplldgbxlatc9gfs.jpg',0,2,'products/c4m1qplldgbxlatc9gfs'),(61,11,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149766/products/w5zbehbcjqygtn5a0mog.jpg',1,0,'products/w5zbehbcjqygtn5a0mog'),(62,11,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149767/products/wmpvsx6gxsdq9hz5jiik.jpg',0,1,'products/wmpvsx6gxsdq9hz5jiik'),(63,11,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149767/products/kpl4gyiahjpeiphpjwos.jpg',0,2,'products/kpl4gyiahjpeiphpjwos'),(64,12,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149769/products/t3m04neejkijssawqfsg.jpg',1,0,'products/t3m04neejkijssawqfsg'),(65,12,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149770/products/v62ntawt9pjzgmvnbrzq.jpg',0,1,'products/v62ntawt9pjzgmvnbrzq'),(66,12,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149770/products/dhbnjefvfafjdr2tcudz.jpg',0,2,'products/dhbnjefvfafjdr2tcudz'),(67,13,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149771/products/ta5kbe1fe7uvqbjzn44l.jpg',1,0,'products/ta5kbe1fe7uvqbjzn44l'),(68,13,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149773/products/updjkt52h5lhwe2nx0ui.jpg',0,1,'products/updjkt52h5lhwe2nx0ui'),(69,13,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149773/products/zvg00iz3jruxllj7clnb.jpg',0,2,'products/zvg00iz3jruxllj7clnb'),(70,14,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149775/products/xjqp45uhcyp41reqthef.jpg',1,0,'products/xjqp45uhcyp41reqthef'),(71,14,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149775/products/dhfdopx8hs1p1inm1sgn.jpg',0,1,'products/dhfdopx8hs1p1inm1sgn'),(72,14,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149776/products/orvu31zgz30qcalnk05u.jpg',0,2,'products/orvu31zgz30qcalnk05u'),(73,15,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149777/products/lrdg86cc845x9wxyhkwp.jpg',1,0,'products/lrdg86cc845x9wxyhkwp'),(74,15,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149778/products/yfm7ha0mptbas3hyabqi.jpg',0,1,'products/yfm7ha0mptbas3hyabqi'),(75,15,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149779/products/hheeauubfsr4egpza6lv.jpg',0,2,'products/hheeauubfsr4egpza6lv'),(76,16,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149780/products/zn8yyv2z427abfii4sjl.jpg',1,0,'products/zn8yyv2z427abfii4sjl'),(77,16,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149781/products/kxvs7ouihuobuxlh3gcp.jpg',0,1,'products/kxvs7ouihuobuxlh3gcp'),(78,16,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149783/products/trhtqmmxyv5wi6hwande.jpg',0,2,'products/trhtqmmxyv5wi6hwande'),(79,17,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149784/products/tmasifwlojtehhfavkee.jpg',1,0,'products/tmasifwlojtehhfavkee'),(80,17,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149785/products/u2yehkp6vs3pnexdq05w.jpg',0,1,'products/u2yehkp6vs3pnexdq05w'),(81,17,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149785/products/ykj47eognm31mlgsfzsl.jpg',0,2,'products/ykj47eognm31mlgsfzsl'),(82,19,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149787/products/hdybg82hufukeyti9apa.jpg',1,0,'products/hdybg82hufukeyti9apa'),(83,19,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149787/products/u5yaswnvxttepjd2exnt.jpg',0,1,'products/u5yaswnvxttepjd2exnt'),(84,19,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149788/products/nhaocrcgiuikzkupwsf5.jpg',0,2,'products/nhaocrcgiuikzkupwsf5'),(85,20,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149789/products/clkoiieerwpmt1lsu1bw.jpg',1,0,'products/clkoiieerwpmt1lsu1bw'),(86,20,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149790/products/ocrfskjmp0dhsxs0knxx.jpg',0,1,'products/ocrfskjmp0dhsxs0knxx'),(87,20,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149791/products/vxkxkxptzlpcwqduaaps.jpg',0,2,'products/vxkxkxptzlpcwqduaaps'),(88,21,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149792/products/dtuufjjugjpjxzkv5f9t.jpg',1,0,'products/dtuufjjugjpjxzkv5f9t'),(89,21,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149792/products/piq9rlbk7kdegwynfbhb.jpg',0,1,'products/piq9rlbk7kdegwynfbhb'),(90,21,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149793/products/hdwinumkyjobrm4nf49e.jpg',0,2,'products/hdwinumkyjobrm4nf49e'),(91,22,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149794/products/osisfyk9dkhvo1r3ooat.jpg',1,0,'products/osisfyk9dkhvo1r3ooat'),(92,22,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149795/products/zmtfupu2fwv5za03h2zq.jpg',0,1,'products/zmtfupu2fwv5za03h2zq'),(93,22,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149796/products/frrnytzpiw1azo0zvp9o.jpg',0,2,'products/frrnytzpiw1azo0zvp9o'),(94,23,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149797/products/nlh1ave6dwszt0yfsq2v.jpg',1,0,'products/nlh1ave6dwszt0yfsq2v'),(95,23,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149798/products/sp6ybjtqy7s0kakyhwxg.jpg',0,1,'products/sp6ybjtqy7s0kakyhwxg'),(96,23,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149799/products/ccxtkr4r2e0cmdm6cmar.jpg',0,2,'products/ccxtkr4r2e0cmdm6cmar'),(97,27,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149801/products/pmbtzebgpmm4o8yt2v4c.jpg',1,0,'products/pmbtzebgpmm4o8yt2v4c'),(98,27,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149802/products/bxfaz9ayjtj2um5wxqim.jpg',0,1,'products/bxfaz9ayjtj2um5wxqim'),(99,27,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149803/products/ciqziahkazqrlhm1gywv.jpg',0,2,'products/ciqziahkazqrlhm1gywv'),(100,28,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149804/products/d0whlbut9z2iilmdyjt5.jpg',1,0,'products/d0whlbut9z2iilmdyjt5'),(101,28,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149804/products/n4iwymjja7q5vkliag2f.jpg',0,1,'products/n4iwymjja7q5vkliag2f'),(102,28,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149805/products/wxyyddwqtvgbtbniex9c.jpg',0,2,'products/wxyyddwqtvgbtbniex9c'),(106,30,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149809/products/fxtertwpfvoozyyaqqp3.jpg',1,0,'products/fxtertwpfvoozyyaqqp3'),(107,30,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149810/products/jseshypuy4pfmahi4ew2.jpg',0,1,'products/jseshypuy4pfmahi4ew2'),(108,33,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149813/products/x8jwqstyqjaorfouwsfn.jpg',1,0,'products/x8jwqstyqjaorfouwsfn'),(109,33,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149813/products/rstx1ee8kpniuutroba7.jpg',0,1,'products/rstx1ee8kpniuutroba7'),(110,33,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149814/products/unn8vnkusa93ajy0ghso.jpg',0,2,'products/unn8vnkusa93ajy0ghso'),(111,35,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149816/products/lskjkj4tilofuu6tbhfb.jpg',1,0,'products/lskjkj4tilofuu6tbhfb'),(112,35,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149817/products/edswwebguwnctleaiaxm.jpg',0,1,'products/edswwebguwnctleaiaxm'),(113,35,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149817/products/vdpjrnerfnqggdzgkc7j.jpg',0,2,'products/vdpjrnerfnqggdzgkc7j'),(114,36,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149818/products/ajmbzffypesvipe4b0xk.jpg',1,0,'products/ajmbzffypesvipe4b0xk'),(115,36,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149819/products/gbvs0flkfun5uakyaoko.jpg',0,1,'products/gbvs0flkfun5uakyaoko'),(116,36,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149820/products/brzvyhg3zr1hjuicnr69.jpg',0,2,'products/brzvyhg3zr1hjuicnr69'),(117,37,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149821/products/y2iih0s2zjg2gemjoqtt.jpg',1,0,'products/y2iih0s2zjg2gemjoqtt'),(118,37,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149822/products/onrwjkzeethfwfidfsx7.jpg',0,1,'products/onrwjkzeethfwfidfsx7'),(119,37,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149822/products/eetve78xrrtww5j2qke6.jpg',0,2,'products/eetve78xrrtww5j2qke6'),(120,38,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149825/products/esbkh0wr0wp7edom4bqb.jpg',1,0,'products/esbkh0wr0wp7edom4bqb'),(121,38,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149825/products/pmpjn1dnt3qxpyfehiob.jpg',0,1,'products/pmpjn1dnt3qxpyfehiob'),(122,38,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149826/products/vov8ldygsap0y8xe1nlx.jpg',0,2,'products/vov8ldygsap0y8xe1nlx'),(123,39,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149828/products/v2ridqy1nsf5thwgxmbk.jpg',1,0,'products/v2ridqy1nsf5thwgxmbk'),(124,39,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149829/products/rm9m6hjwux1z84rad1ro.jpg',0,1,'products/rm9m6hjwux1z84rad1ro'),(125,39,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149829/products/zkmiou0t0hduljnu6nr2.jpg',0,2,'products/zkmiou0t0hduljnu6nr2'),(126,40,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149830/products/ilhd9undcgmhacillmko.jpg',1,0,'products/ilhd9undcgmhacillmko'),(127,40,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149831/products/zkx93pbpbe8fey0tsdab.jpg',0,1,'products/zkx93pbpbe8fey0tsdab'),(128,40,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149834/products/cjaevgzolo3jgeqakaxc.jpg',0,2,'products/cjaevgzolo3jgeqakaxc'),(129,41,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149836/products/jh574df4y0szmrz1a4b8.jpg',1,0,'products/jh574df4y0szmrz1a4b8'),(130,41,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149836/products/nbi321bem9qohrhketb6.jpg',0,1,'products/nbi321bem9qohrhketb6'),(131,41,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149837/products/zmjedwcmjvxhspig5oeg.jpg',0,2,'products/zmjedwcmjvxhspig5oeg'),(132,43,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149839/products/pg8kocvkdlcx5uobxssx.jpg',1,0,'products/pg8kocvkdlcx5uobxssx'),(133,43,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149839/products/pttwi3xrxp32u9phdq7w.jpg',0,1,'products/pttwi3xrxp32u9phdq7w'),(134,43,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149840/products/vzuwsrfwqu1mopyk0hfm.jpg',0,2,'products/vzuwsrfwqu1mopyk0hfm'),(135,45,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149842/products/lxdonufwhvcwrlsdcxyc.jpg',1,0,'products/lxdonufwhvcwrlsdcxyc'),(136,45,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149842/products/zla2zybu7sx9hwxrixfx.jpg',0,1,'products/zla2zybu7sx9hwxrixfx'),(137,45,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149843/products/ytob06tubkiro4ouhmo9.jpg',0,2,'products/ytob06tubkiro4ouhmo9'),(138,46,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149846/products/m7q7dptodo5bxzxew5cw.jpg',1,0,'products/m7q7dptodo5bxzxew5cw'),(139,46,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149846/products/yy46kbzdpr6bwyywylc8.jpg',0,1,'products/yy46kbzdpr6bwyywylc8'),(140,46,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149847/products/v7gdin4enqndydt82tsj.jpg',0,2,'products/v7gdin4enqndydt82tsj'),(141,47,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149848/products/gc318mmfdtpusixmstsz.jpg',1,0,'products/gc318mmfdtpusixmstsz'),(142,47,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149849/products/jj0r9np8mco6dpb78gos.jpg',0,1,'products/jj0r9np8mco6dpb78gos'),(143,47,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149849/products/tpgxjjgw5qzq6izas3vh.jpg',0,2,'products/tpgxjjgw5qzq6izas3vh'),(144,49,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149851/products/pc33mggcsivvtnfla11s.jpg',1,0,'products/pc33mggcsivvtnfla11s'),(145,49,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149852/products/y9xjai7gavkuerxqjfrx.jpg',0,1,'products/y9xjai7gavkuerxqjfrx'),(146,49,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149853/products/gosatem455u3vl5hmunm.jpg',0,2,'products/gosatem455u3vl5hmunm'),(147,50,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149854/products/nftp1hxuxakvlhiz25ji.jpg',1,0,'products/nftp1hxuxakvlhiz25ji'),(148,50,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149855/products/bnig8pozbzaify8tudnx.jpg',0,1,'products/bnig8pozbzaify8tudnx'),(149,50,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149855/products/smqspevl5xr9hko9s0vm.jpg',0,2,'products/smqspevl5xr9hko9s0vm'),(150,51,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149857/products/kujyvjrcbrj80cifa5d6.jpg',1,0,'products/kujyvjrcbrj80cifa5d6'),(151,51,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149857/products/wdt6v6arjpstu14dmyst.jpg',0,1,'products/wdt6v6arjpstu14dmyst'),(152,51,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149858/products/doowx09y7jzyofffv94n.jpg',0,2,'products/doowx09y7jzyofffv94n'),(153,52,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149859/products/syzfofiltpokdmkvmpu9.jpg',1,0,'products/syzfofiltpokdmkvmpu9'),(154,52,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149860/products/n55fag7biwpbvjbnajln.jpg',0,1,'products/n55fag7biwpbvjbnajln'),(155,52,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149860/products/a3uzgfkonuomafl6otea.jpg',0,2,'products/a3uzgfkonuomafl6otea'),(156,54,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149862/products/fd2dpfeb5valz88ie2k2.jpg',1,0,'products/fd2dpfeb5valz88ie2k2'),(157,54,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149863/products/ix0qlnzkgwbthaqd5pmw.jpg',0,1,'products/ix0qlnzkgwbthaqd5pmw'),(158,54,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149863/products/klxbr3ed4x2np8wosc0v.jpg',0,2,'products/klxbr3ed4x2np8wosc0v'),(159,55,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149865/products/nxv7rb0tnf27bhplrrmf.jpg',1,0,'products/nxv7rb0tnf27bhplrrmf'),(160,55,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149866/products/ekgtovgxpwff7voi6pxn.jpg',0,1,'products/ekgtovgxpwff7voi6pxn'),(161,55,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149867/products/rlbjemrhpqwdisp87ofu.jpg',0,2,'products/rlbjemrhpqwdisp87ofu'),(162,56,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149868/products/mkk1mcmw0bsvnntso3bz.jpg',1,0,'products/mkk1mcmw0bsvnntso3bz'),(163,56,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149869/products/geznbomtybusx5dtnsy0.jpg',0,1,'products/geznbomtybusx5dtnsy0'),(164,56,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149869/products/xjqbzfbhf92ch5yjs70u.jpg',0,2,'products/xjqbzfbhf92ch5yjs70u'),(165,3,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245649/products/pttimouco2tvvv9u8j8e.jpg',1,0,'products/pttimouco2tvvv9u8j8e'),(166,3,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245650/products/hehsvsrockjk280wtctp.jpg',0,1,'products/hehsvsrockjk280wtctp'),(167,3,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245651/products/jkkyzu3gqntynhabtsid.jpg',0,2,'products/jkkyzu3gqntynhabtsid'),(168,4,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245652/products/cyobqd16j4tnjawaynrl.jpg',1,0,'products/cyobqd16j4tnjawaynrl'),(169,4,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245653/products/ey9c2dx3dtfytl5idhga.jpg',0,1,'products/ey9c2dx3dtfytl5idhga'),(170,4,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245655/products/l40kyhvysqmu90fob37v.jpg',0,2,'products/l40kyhvysqmu90fob37v'),(171,5,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245656/products/iezc2b83tj5vkroi6hua.jpg',1,0,'products/iezc2b83tj5vkroi6hua'),(172,5,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245657/products/pj3bvbml2jzlum6j8uwf.jpg',0,1,'products/pj3bvbml2jzlum6j8uwf'),(173,5,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245658/products/nan3lcxjxdv0idqn9yrw.jpg',0,2,'products/nan3lcxjxdv0idqn9yrw'),(174,6,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245660/products/gqwttbbvevdcytumsvnk.jpg',1,0,'products/gqwttbbvevdcytumsvnk'),(175,6,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245660/products/vtbtrritiemifuaqjdfi.jpg',0,1,'products/vtbtrritiemifuaqjdfi'),(176,6,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245661/products/pe1f1ducfqbmi15cyzvm.jpg',0,2,'products/pe1f1ducfqbmi15cyzvm'),(177,7,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245662/products/zz9ia9n63zgkc13bqbdb.jpg',1,0,'products/zz9ia9n63zgkc13bqbdb'),(178,7,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245663/products/pmwa0yjbxmixm386rbss.jpg',0,1,'products/pmwa0yjbxmixm386rbss'),(179,7,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776245664/products/x41duj0q1yk9w7n2rre5.jpg',0,2,'products/x41duj0q1yk9w7n2rre5'),(180,29,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149807/products/ch9zmb0f7rnukqanlpcj.jpg',1,0,'products/ch9zmb0f7rnukqanlpcj'),(181,29,'https://res.cloudinary.com/dlwf1izos/image/upload/v1776149808/products/pasrvcwc3m4eacj4wpg1.jpg',0,2,'products/pasrvcwc3m4eacj4wpg1');
/*!40000 ALTER TABLE `product_images` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_reviews`
--

DROP TABLE IF EXISTS `product_reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` text,
  `created_at` datetime(6) DEFAULT NULL,
  `rating` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKi8pvlswx9d9ul91orv429gxf` (`user_id`,`product_id`),
  KEY `FK35kxxqe2g9r4mww80w9e3tnw9` (`product_id`),
  CONSTRAINT `FK35kxxqe2g9r4mww80w9e3tnw9` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FK58i39bhws2hss3tbcvdmrm60f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_reviews`
--

LOCK TABLES `product_reviews` WRITE;
/*!40000 ALTER TABLE `product_reviews` DISABLE KEYS */;
INSERT INTO `product_reviews` VALUES (1,'10đ','2026-04-18 08:26:08.300995',5,NULL,35,2);
/*!40000 ALTER TABLE `product_reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `price` bigint NOT NULL,
  `cpu` varchar(255) DEFAULT NULL,
  `ram` varchar(255) DEFAULT NULL,
  `screen` varchar(255) DEFAULT NULL,
  `operating_system` varchar(255) DEFAULT NULL,
  `battery_capacity` varchar(255) DEFAULT NULL,
  `design` varchar(255) DEFAULT NULL,
  `warranty_info` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `sold_quantity` int DEFAULT NULL,
  `stock_quantity` int DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `brand_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_product_category` (`category_id`),
  KEY `fk_product_brand` (`brand_id`),
  CONSTRAINT `fk_product_brand` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (3,'Macbook Air 13 128GB (2017)',23990000,'Intel Core i5 1.8 Ghz','8 GB','13.3 inch LED','Mac OS','5800mAh','Aluminum ultra thin','12 months','Office laptop',0,100,1,2),(4,'Macbook Air 13 256GB (2017)',28990000,'Intel Core i5 1.8 Ghz','8 GB','13.3 inch LED','Mac OS','6000mAh','Aluminum design','12 months','Good battery life',0,100,1,2),(5,'Macbook 12 256GB (2017)',33990000,'Intel Core M3 1.2 GHz','8 GB','12 inch LED','Mac OS','6000mAh','Premium slim','12 months','Elegant laptop',0,150,1,2),(6,'Macbook Pro 13 128GB (2017)',33990000,'Intel Core i5 2.3 GHz','8 GB','13.3 inch LED','Mac OS','6000mAh','Aluminum slim','12 months','Performance laptop',0,200,1,2),(7,'Macbook Pro 13 Touch Bar (2018)',44990000,'Intel Core i5 2.3GHz','8 GB','13.3 Retina','Mac OS','7000mAh','Aluminum premium','12 months','High performance',0,100,1,2),(8,'MacBook Air M3',27990000,'Apple M3 8-core','8GB Unified','13.6\" Liquid Retina','macOS Sonoma','52.6Wh - 18 giờ','Vỏ nhôm nguyên khối, 1.24kg','12 tháng Apple','MacBook Air M3 mang đến hiệu năng vượt trội với chip M3 mới nhất, màn hình Liquid Retina tuyệt đẹp và thời lượng pin cả ngày.',245,50,1,2),(9,'MacBook Pro 14\" M3 Pro',48990000,'Apple M3 Pro 12-core','18GB Unified','14.2\" Liquid Retina XDR','macOS Sonoma','72.4Wh - 17 giờ','Vỏ nhôm, Space Black, 1.61kg','12 tháng Apple','MacBook Pro 14 inch với chip M3 Pro cho hiệu năng chuyên nghiệp, màn hình XDR siêu sáng và hệ thống âm thanh 6 loa.',189,35,1,2),(10,'MacBook Pro 16\" M3 Max',89990000,'Apple M3 Max 16-core','36GB Unified','16.2\" Liquid Retina XDR','macOS Sonoma','100Wh - 22 giờ','Vỏ nhôm, Space Black, 2.14kg','12 tháng Apple','Cỗ máy mạnh mẽ nhất của Apple dành cho các chuyên gia sáng tạo nội dung, phát triển phần mềm và xử lý đồ họa nặng.',67,15,1,2),(11,'ASUS ROG Strix G16',35990000,'Intel Core i9-13980HX','16GB DDR5','16\" QHD+ 240Hz','Windows 11','90Wh - 8 giờ','Eclipse Gray, RGB keyboard, 2.5kg','24 tháng ASUS','Laptop gaming cao cấp với card RTX 4070, tần số quét 240Hz và hệ thống tản nhiệt tiên tiến cho trải nghiệm gaming đỉnh cao.',312,28,1,3),(12,'ASUS ZenBook 14 OLED',22990000,'Intel Core Ultra 7 155H','16GB LPDDR5x','14\" 2.8K OLED 120Hz','Windows 11','75Wh - 15 giờ','Ponder Blue, 1.28kg siêu nhẹ','24 tháng ASUS','Ultrabook cao cấp với màn hình OLED 2.8K sắc nét, chip Intel thế hệ mới và thiết kế siêu mỏng nhẹ chỉ 1.28kg.',198,42,1,3),(13,'ASUS TUF Gaming A15',19990000,'AMD Ryzen 7 7735HS','16GB DDR5','15.6\" FHD 144Hz','Windows 11','76Wh - 10 giờ','Graphite Black, MIL-STD-810H, 2.2kg','24 tháng ASUS','Laptop gaming bền bỉ đạt chuẩn quân sự, trang bị RTX 4060 và tản nhiệt kép hiệu quả.',456,65,1,3),(14,'Dell XPS 15',42990000,'Intel Core i7-13700H','32GB DDR5','15.6\" 3.5K OLED','Windows 11 Pro','86Wh - 13 giờ','Platinum Silver, InfinityEdge, 1.86kg','12 tháng Dell','Laptop cao cấp với màn hình InfinityEdge không viền, chip Intel thế hệ 13 và thiết kế sang trọng cho doanh nhân.',134,22,1,5),(15,'Dell Inspiron 16',16990000,'Intel Core i5-1340P','16GB DDR4','16\" FHD+ WVA','Windows 11','54Wh - 9 giờ','Carbon Black, 1.87kg','12 tháng Dell','Laptop văn phòng màn hình 16 inch rộng rãi, hiệu năng ổn định cho công việc hàng ngày và giải trí.',523,78,1,5),(16,'Dell Gaming G15',24990000,'Intel Core i7-13650HX','16GB DDR5','15.6\" FHD 165Hz','Windows 11','86Wh - 10 giờ','Dark Shadow, tản nhiệt kép, 2.65kg','12 tháng Dell','Laptop gaming tầm trung mạnh mẽ với RTX 4060, màn hình 165Hz và tản nhiệt hiệu quả.',287,33,1,5),(17,'HP Spectre x360 14',38990000,'Intel Core Ultra 7 155H','16GB LPDDR5x','14\" 2.8K OLED Touch','Windows 11','68Wh - 15 giờ','Nightfall Black, xoay 360°, 1.4kg','12 tháng HP','Laptop 2-in-1 cao cấp với màn hình OLED cảm ứng xoay 360 độ, bút stylus và thiết kế sang trọng.',156,25,1,6),(18,'HP Victus 16',18990000,'AMD Ryzen 5 7535HS','8GB DDR5','16.1\" FHD 144Hz','Windows 11','70Wh - 8 giờ','Mica Silver, 2.3kg','12 tháng HP','Laptop gaming giá rẻ với RTX 4050, phù hợp cho sinh viên và game thủ tầm trung.',678,90,1,6),(19,'HP EliteBook 840 G10',32990000,'Intel Core i7-1365U','16GB DDR5','14\" WUXGA IPS','Windows 11 Pro','51Wh - 14 giờ','Silver, vỏ nhôm, 1.36kg','36 tháng HP','Laptop doanh nghiệp cao cấp với bảo mật HP Wolf Security, chip vPro và bền bỉ chuẩn MIL-STD.',89,18,1,6),(20,'Lenovo ThinkPad X1 Carbon Gen 11',41990000,'Intel Core i7-1365U vPro','16GB LPDDR5','14\" 2.8K OLED','Windows 11 Pro','57Wh - 15 giờ','Deep Black, 1.12kg siêu nhẹ','36 tháng Lenovo','Laptop doanh nhân hàng đầu thế giới, siêu nhẹ 1.12kg, bàn phím tốt nhất trong phân khúc.',167,20,1,7),(21,'Lenovo Legion Pro 5',39990000,'AMD Ryzen 9 7945HX','32GB DDR5','16\" WQXGA 240Hz','Windows 11','99.99Wh - 9 giờ','Onyx Grey, tản nhiệt ColdFront, 2.5kg','24 tháng Lenovo','Laptop gaming hiệu năng cao với RTX 4070, màn hình 240Hz và pin gần 100Wh cho gaming dài.',234,27,1,7),(22,'Lenovo IdeaPad Slim 5',14990000,'AMD Ryzen 5 7530U','16GB DDR4','14\" FHD IPS','Windows 11','56.5Wh - 12 giờ','Cloud Grey, 1.46kg','24 tháng Lenovo','Laptop sinh viên giá tốt với hiệu năng ổn định, pin trâu và thiết kế gọn nhẹ.',892,120,1,7),(23,'MSI Raider GE78 HX',72990000,'Intel Core i9-14900HX','32GB DDR5','17\" UHD+ 144Hz Mini LED','Windows 11','99.99Wh - 7 giờ','Titanium Blue, Per-Key RGB, 3.1kg','24 tháng MSI','Siêu laptop gaming với RTX 4080, màn hình Mini LED 4K và hệ thống tản nhiệt Phase Change Liquid Metal.',45,8,1,8),(24,'MSI Modern 15',12990000,'Intel Core i5-1335U','8GB DDR4','15.6\" FHD IPS','Windows 11','39.3Wh - 7 giờ','Urban Silver, 1.7kg','24 tháng MSI','Laptop văn phòng mỏng nhẹ, phù hợp cho công việc hàng ngày với mức giá phải chăng.',345,55,1,8),(25,'MSI Stealth 16 Studio',52990000,'Intel Core i7-13700H','32GB DDR5','16\" QHD+ 240Hz OLED','Windows 11 Pro','82Wh - 9 giờ','Star Blue, CNC nhôm, 1.88kg','24 tháng MSI','Laptop sáng tạo nội dung với RTX 4070, màn hình OLED 240Hz và thiết kế mỏng chỉ 19.95mm.',78,12,1,8),(26,'Acer Predator Helios 16',44990000,'Intel Core i9-13900HX','32GB DDR5','16\" WQXGA 240Hz','Windows 11','90Wh - 8 giờ','Abyssal Black, 3D AeroBlade Fan, 2.6kg','24 tháng Acer','Laptop gaming flagship với RTX 4080, tản nhiệt 3D AeroBlade và bàn phím MagForce từ tính.',112,15,1,4),(27,'Acer Swift Go 14',18990000,'Intel Core Ultra 5 125H','16GB LPDDR5x','14\" 2.8K OLED','Windows 11','65Wh - 13 giờ','Moonstone White, 1.3kg','12 tháng Acer','Ultrabook nhẹ chỉ 1.3kg với màn hình OLED 2.8K và chip Intel AI thế hệ mới.',267,38,1,4),(28,'PC Gaming RTX 4070 Super',28990000,'Intel Core i5-14600K','32GB DDR5 5600MHz','Không kèm màn hình','Windows 11','PSU 750W 80+ Gold','Case NZXT H5 Flow, RGB','24 tháng linh kiện','Bộ PC Gaming cao cấp với RTX 4070 Super, SSD 1TB NVMe và tản nhiệt AIO 240mm.',189,20,10,3),(29,'PC Văn phòng Core i3',8990000,'Intel Core i3-12100','8GB DDR4 3200MHz','Không kèm màn hình','Windows 11','PSU 450W 80+','Case Compact mATX, gọn gàng','24 tháng linh kiện','Bộ PC văn phòng giá rẻ, đủ mạnh cho Word, Excel và duyệt web hàng ngày.',567,45,10,5),(30,'Apple iMac 24\" M3',35990000,'Apple M3 8-core','8GB Unified','24\" 4.5K Retina','macOS Sonoma','Nguồn tích hợp','Midnight, thiết kế all-in-one 11.5mm','12 tháng Apple','iMac M3 với màn hình 4.5K Retina 24 inch, thiết kế all-in-one siêu mỏng và hệ thống âm thanh 6 loa.',145,18,10,2),(31,'PC Gaming RTX 4060',18990000,'AMD Ryzen 5 5600','16GB DDR4 3200MHz','Không kèm màn hình','Windows 11','PSU 650W 80+ Bronze','Case MSI MAG Forge, ARGB','24 tháng linh kiện','PC Gaming tầm trung với RTX 4060 8GB, chạy mượt mọi game ở 1080p.',334,32,10,8),(32,'PC Workstation Xeon',45990000,'Intel Xeon W-1390P','64GB DDR5 ECC','Không kèm màn hình','Windows 11 Pro','PSU 850W 80+ Titanium','Case Fractal Design Meshify, tĩnh lặng','36 tháng','Máy trạm chuyên nghiệp cho dựng phim, render 3D với Quadro RTX A4000 và RAM ECC.',34,5,10,6),(33,'AirPods Pro 2 USB-C',5990000,'Chip H2','N/A','N/A','iOS/macOS/Android','6 giờ (30 giờ với case)','In-ear, chống ồn chủ động','12 tháng Apple','Tai nghe true wireless cao cấp với ANC thế hệ 2, âm thanh không gian và cổng USB-C tiện lợi.',1235,199,8,2),(34,'Balo laptop ASUS ROG Ranger',2490000,'N/A','N/A','N/A','N/A','N/A','Polyester chống nước, ngăn laptop 17\"','12 tháng ASUS','Balo gaming chống nước với ngăn laptop 17 inch, nhiều ngăn phụ và đệm lưng thoáng khí.',567,80,8,3),(35,'Sạc dự phòng Anker 20000mAh',890000,'N/A','N/A','Màn hình LED','N/A','20000mAh, PD 65W','Compact, mặt nhám chống trơn','18 tháng Anker','Sạc dự phòng 20000mAh sạc nhanh PD 65W, đủ sạc laptop qua USB-C.',2348,297,8,4),(36,'Webcam Logitech C920 HD Pro',1890000,'N/A','N/A','N/A','Windows/Mac/Chrome OS','Nguồn USB','Full HD 1080p, 2 mic stereo','24 tháng Logitech','Webcam Full HD 1080p cho họp online và streaming, 2 mic stereo khử ồn.',678,95,8,13),(37,'Hub USB-C 7in1 Ugreen',690000,'N/A','N/A','N/A','Mọi hệ điều hành','Nguồn USB-C PD 100W pass-through','Nhôm CNC, compact 10x3cm','18 tháng','Hub USB-C 7 cổng: HDMI 4K, USB 3.0 x3, SD/TF, PD 100W cho laptop.',1457,249,8,4),(38,'LG UltraGear 27GP850-B',9990000,'N/A','N/A','27\" QHD Nano IPS 165Hz','N/A','Nguồn AC','Chân đế ergonomic, HDMI 2.1','36 tháng LG','Màn hình gaming 27 inch QHD IPS 165Hz, 1ms, HDR400 và NVIDIA G-Sync Compatible.',345,40,11,11),(39,'Samsung Odyssey G5 32\"',7490000,'N/A','N/A','32\" QHD VA 165Hz Curved','N/A','Nguồn AC','Cong 1000R, FreeSync Premium','24 tháng Samsung','Màn hình cong gaming 32 inch QHD 165Hz, độ cong 1000R immersive.',234,30,11,12),(40,'Dell UltraSharp U2723QE',12990000,'N/A','N/A','27\" 4K IPS Black','N/A','Nguồn AC','USB-C 90W, KVM, IPS Black Tech','36 tháng Dell','Màn hình chuyên nghiệp 4K IPS Black với USB-C 90W, tỷ lệ tương phản 2000:1 và 98% DCI-P3.',123,15,11,5),(41,'ASUS ProArt PA278QV',8490000,'N/A','N/A','27\" QHD IPS 75Hz','N/A','Nguồn AC','Xoay, nghiêng, nâng hạ, 100% sRGB','36 tháng ASUS','Màn hình thiết kế đồ họa 27 inch với 100% sRGB, đã hiệu chỉnh màu từ nhà máy.',189,22,11,3),(42,'Logitech MX Keys S',2690000,'N/A','N/A','Đèn nền thông minh','Windows/Mac/Linux','Pin sạc, 10 ngày','Low-profile, phím Perfect Stroke','24 tháng Logitech','Bàn phím wireless cao cấp với phím Perfect Stroke, đèn nền tự động và kết nối 3 thiết bị.',456,60,12,13),(43,'Logitech MX Master 3S',2490000,'N/A','N/A','N/A','Windows/Mac/Linux','Pin sạc USB-C, 70 ngày','Ergonomic, 8000 DPI, MagSpeed','24 tháng Logitech','Chuột wireless cao cấp nhất với cuộn MagSpeed, cảm biến 8000 DPI và click yên lặng.',567,75,12,13),(44,'ASUS ROG Azoth',5490000,'N/A','N/A','Màn hình OLED trên bàn phím','Windows','Pin 4000mAh, 2000 giờ','75%, Gasket-mount, hot-swap, NX Snow','24 tháng ASUS','Bàn phím cơ gaming wireless 75% với màn hình OLED, gasket-mount và switch hot-swap.',234,28,12,3),(45,'Razer DeathAdder V3',1890000,'N/A','N/A','N/A','Windows','Có dây USB','Ergonomic, 30000 DPI, 59g siêu nhẹ','24 tháng','Chuột gaming ergonomic siêu nhẹ 59g với cảm biến Focus Pro 30K DPI.',789,100,12,4),(46,'Lenovo Yoga 9i',36990000,'Intel Core i7-1360P','16GB LPDDR5','14\" 4K OLED Touch','Windows 11','75Wh - 14 giờ','Xoay 360°, Bowers & Wilkins, 1.4kg','24 tháng Lenovo','Laptop 2-in-1 cao cấp với màn hình 4K OLED, loa Bowers & Wilkins và bút stylus.',123,16,1,7),(47,'Acer Nitro V 15',16990000,'Intel Core i5-13420H','16GB DDR5','15.6\" FHD 144Hz','Windows 11','57Wh - 7 giờ','Obsidian Black, 2.1kg','12 tháng Acer','Laptop gaming giá rẻ nhất phân khúc RTX 4050 với hiệu năng tốt cho sinh viên.',789,95,1,4),(48,'LG Gram 16',29990000,'Intel Core i7-1360P','16GB LPDDR5','16\" WQXGA IPS','Windows 11','80Wh - 22 giờ','White, siêu nhẹ 1.19kg','12 tháng LG','Laptop siêu nhẹ nhất 16 inch chỉ 1.19kg với pin 80Wh dùng cả ngày.',156,19,1,11),(49,'Samsung Galaxy Book4 Pro',31990000,'Intel Core Ultra 7 155H','16GB LPDDR5x','16\" 3K AMOLED 120Hz','Windows 11','76Wh - 18 giờ','Moonstone Gray, 1.56kg','12 tháng Samsung','Laptop AMOLED 3K 120Hz từ Samsung với chip Intel AI và thiết kế siêu mỏng.',98,14,1,12),(50,'LG UltraFine 32UN880-B',15990000,'N/A','N/A','32\" 4K UHD IPS','N/A','Nguồn AC','Arm Ergo, USB-C, HDR10','36 tháng LG','Màn hình 4K với chân đế Ergo kẹp bàn, USB-C 60W và HDR10 cho thiết kế đồ họa.',89,12,11,11),(51,'Tai nghe Sony WH-1000XM5',7490000,'N/A','N/A','N/A','iOS/Android','30 giờ','Over-ear, ANC cao cấp, 250g','12 tháng Sony','Tai nghe chống ồn tốt nhất thế giới với 30 giờ pin, âm thanh Hi-Res và 8 mic ANC.',678,85,8,12),(52,'Bàn phím Apple Magic Keyboard',3490000,'N/A','N/A','N/A','macOS/iPadOS','Pin sạc Lightning, 1 tháng','Nhôm, Touch ID, Full-size','12 tháng Apple','Bàn phím không dây Apple với Touch ID, bàn phím số và kết nối Bluetooth.',345,50,12,2),(53,'PC Mini Intel NUC 13 Pro',14990000,'Intel Core i7-1365U','16GB DDR4','Không kèm màn hình','Windows 11 Pro','Nguồn 120W adapter','Mini PC 11.7x11.2x5.4cm','24 tháng Intel','Mini PC siêu nhỏ gọn cho văn phòng, hỗ trợ 4 màn hình và Thunderbolt 4.',234,28,10,5),(54,'Màn hình Samsung ViewFinity S8',10990000,'N/A','N/A','27\" 4K UHD IPS','N/A','Nguồn AC','USB-C 90W, KVM, Matte Display','36 tháng Samsung','Màn hình 4K chuyên nghiệp với USB-C 90W, HDR và 98% DCI-P3 cho sáng tạo nội dung.',167,20,11,12),(55,'Ổ cứng SSD Samsung 990 Pro 2TB',4890000,'N/A','N/A','N/A','Mọi hệ điều hành','N/A','M.2 NVMe PCIe 4.0, 7450MB/s','60 tháng Samsung','SSD NVMe nhanh nhất với tốc độ đọc 7450MB/s, bảo hành 5 năm.',1234,180,8,12),(56,'Apple Magic Mouse',2290000,'N/A','N/A','N/A','macOS','Pin sạc Lightning, 1 tháng','Multi-touch, 99g, mặt kính','12 tháng Apple','Chuột không dây Apple với bề mặt Multi-Touch, thiết kế tối giản và pin dùng 1 tháng.',456,65,12,2),(57,'Dell Alienware AW3225QF',28990000,'N/A','N/A','32\" 4K QD-OLED 240Hz','N/A','Nguồn AC','QD-OLED, HDMI 2.1, DP 2.1','36 tháng Dell','Màn hình gaming đỉnh cao QD-OLED 4K 240Hz với 0.03ms và True Black 400.',56,7,11,5),(59,'NEURAL CORE-X',45990000,'Neural AI X1 Processor (16 cores, AI optimized)','32GB LPDDR5X','16 inch OLED, 4K, 120Hz','NeuralOS v3 (AI-integrated)','9000mAh, fast charge 120W','Ultra-thin aluminum chassis, futuristic minimal design','24 months official warranty','NEURAL CORE-X là thiết bị điện toán thế hệ mới tích hợp AI mạnh mẽ, tối ưu cho lập trình, thiết kế và xử lý dữ liệu lớn. Hỗ trợ AI real-time, tiết kiệm năng lượng và hiệu năng vượt trội.',0,50,1,8);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ROLE_ADMIN'),(2,'ROLE_CUSTOMER'),(3,'ROLE_SHIPPER'),(4,'ROLE_STAFF');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_user_roles_role` (`role_id`),
  CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,1),(2,2),(4,2),(6,2),(3,3),(5,4);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_vouchers`
--

DROP TABLE IF EXISTS `user_vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_vouchers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `voucher_id` bigint NOT NULL,
  `status` varchar(255) NOT NULL,
  `assigned_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `used_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_voucher_user` (`user_id`),
  KEY `fk_user_voucher_voucher` (`voucher_id`),
  CONSTRAINT `fk_user_voucher_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_user_voucher_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_vouchers`
--

LOCK TABLES `user_vouchers` WRITE;
/*!40000 ALTER TABLE `user_vouchers` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_vouchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Đỗ Việt Cường','admin@gmail.com','$2a$10$/VFMNUPBKNVRMjxPFCYKZ.lKahoLQda0EaAxdqoun1w3DqwNLa2me','123456789',NULL),(2,'Đỗ Việt Cường','member@gmail.com','$2a$10$j7Upgupou72GBmukz0G6pOATk3wlCAgaoFCEqAhSvLToD/V/1wlpu',NULL,NULL),(3,'Đỗ Việt Cường','shipper@gmail.com','$2a$10$u2B29HDxuWVYY3fUJ8R2qunNzXngfxij5GpvlFAEtIz3JpK/WFXF2',NULL,NULL),(4,'Vi Hải Anh','nguoidung@gmail.com','$2a$10$ZCqCO9gSWt8A8HNXAWq8luqfNbJm0uG3PsUlzry0aRLwO3VHQZTmy','123456','Ha Noi'),(5,'Đỗ Việt Cường','staff@gmail.com','$2a$10$u2B29HDxuWVYY3fUJ8R2qunNzXngfxij5GpvlFAEtIz3JpK/WFXF2',NULL,NULL),(6,'Nguyen Van B','newuser@gmail.com','$2a$10$xM3hTm5dCQr/vApS0lxitOtCGcTjCkAbwJFmv/TmXLyQM9Ikf45i.',NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `voucher_usage_history`
--

DROP TABLE IF EXISTS `voucher_usage_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `voucher_usage_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `voucher_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `action` varchar(255) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `voucher_usage_history`
--

LOCK TABLES `voucher_usage_history` WRITE;
/*!40000 ALTER TABLE `voucher_usage_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `voucher_usage_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vouchers`
--

DROP TABLE IF EXISTS `vouchers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vouchers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `discount_type` varchar(255) NOT NULL,
  `discount_value` double NOT NULL,
  `min_order_value` double DEFAULT '0',
  `start_date` datetime NOT NULL,
  `end_date` datetime NOT NULL,
  `active` tinyint(1) DEFAULT '1',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vouchers`
--

LOCK TABLES `vouchers` WRITE;
/*!40000 ALTER TABLE `vouchers` DISABLE KEYS */;
INSERT INTO `vouchers` VALUES (1,'ABC','FIXED',10000,0,'2026-04-20 00:00:00','2026-04-22 00:00:00',1,'2026-04-21 16:03:46');
/*!40000 ALTER TABLE `vouchers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'mydb'
--

--
-- Dumping routines for database 'mydb'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-29 16:58:42
