package com.talool.mobile.android.activity;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import com.talool.api.thrift.DealAcquire_t;
import com.talool.api.thrift.Merchant_t;
import com.talool.mobile.android.R;
import com.talool.mobile.android.util.ImageDownloader;
import com.talool.mobile.android.util.ThriftHelper;
import com.talool.mobile.android.util.TypefaceFactory;
import com.talool.thrift.util.ThriftUtil;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DealActivity extends Activity {
	private static ThriftHelper client;
	private DealAcquire_t deal;
	private Merchant_t merchant;
	private ImageView dealMerchantImage;
	private ImageView logoImageView;
	private TextView dealAddressText;
	private TextView dealSummaryText;
	private TextView dealValidText;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.deal_activity_layout);
		createThriftClient();
		logoImageView = (ImageView) findViewById(R.id.dealLogoImage);
		dealMerchantImage = (ImageView) findViewById(R.id.dealMerchantImage);
		dealAddressText = (TextView) findViewById(R.id.dealAddressText);
		dealValidText = (TextView) findViewById(R.id.dealValidText);
		dealSummaryText = (TextView) findViewById(R.id.dealSummaryText);
		

		try {
			byte[] dealBytes = (byte[]) getIntent().getSerializableExtra("deal");
			byte[] merchantBytes = (byte[]) getIntent().getSerializableExtra("merchant");

			deal = new DealAcquire_t();
			merchant = new Merchant_t();
			ThriftUtil.deserialize(dealBytes,deal);
			ThriftUtil.deserialize(merchantBytes,merchant);

			setText();
			loadImages();
			
		} catch (TException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void loadImages()
	{
		if(deal.deal.imageUrl != null){
			ImageDownloader imageTask = new ImageDownloader(this.dealMerchantImage);
			imageTask.execute(new String[]{deal.deal.imageUrl});			
		}

		if(merchant.locations.get(0).logoUrl != null){
			ImageDownloader imageTask = new ImageDownloader(this.logoImageView);
			imageTask.execute(new String[]{merchant.locations.get(0).logoUrl});			
		}

	}

	private void setText()
	{
		setAddressText();
		dealSummaryText.setText(deal.deal.summary);
		dealValidText.setText(deal.deal.details);
		setTitle(merchant.name);
		
		final TextView useDealIcon = (TextView) findViewById(R.id.useDealIcon);
		useDealIcon.setTypeface(TypefaceFactory.get().getFontAwesome());
		final TextView giftIcon = (TextView) findViewById(R.id.giftIcon);
		giftIcon.setTypeface(TypefaceFactory.get().getFontAwesome());

	}
	
	private void setAddressText()
	{
		String str = merchant.locations.get(0).address.address1;
		if(merchant.locations.get(0).address.address2 != null )
		{
			str = str + "\n" + merchant.locations.get(0).address.address2 ;
		}
		else
		{
			str = str + "\n" + merchant.locations.get(0).address.city +  " "
					+ merchant.locations.get(0).address.stateProvinceCounty + " " 
					+ merchant.locations.get(0).address.zip;
		}

		dealAddressText.setText(str);
	}

	private void createThriftClient()
	{
		try {
			client = new ThriftHelper();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
