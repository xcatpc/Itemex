package sh.ome.itemex.functions;

public class IPFS {
    private String link_CID;
    private String payout_cid;

    public IPFS(String[] timestamp_price, int blocksize, String jwt) {
        // save data to file

        // upload to ipfs

        // load old masterfile if exist - add new or replace block cid. (blocks with size of bocksize are final

        // upload masterfile to ipfs

        // edit index.html file with cid of masterfile

        // upload index.html to ipfs CID = LINK CID

        this.link_CID = "cid";
    }

    public String get_link(String domain) {
        if(link_CID != null)
            return domain + "/ipfs/" + link_CID;
        else
            return "";
    }


}
