package pmujtmk.yeo.myprice; // Manage the item information

public class Item {

    String ID, imageStr, barcode, name, quantity, price, dis_price, member_price, cart_displayed_price, lastupdate;

    public Item(String ID, String imageStr, String barcode, String name, String quantity, String price, String dis_price, String member_price, String cart_displayed_price, String lastupdate) {
        this.ID = ID;
        this.imageStr = imageStr;
        this.barcode = barcode;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.dis_price = dis_price;
        this.member_price = member_price;
        this.cart_displayed_price = cart_displayed_price;
        this.lastupdate = lastupdate;
    }
}
