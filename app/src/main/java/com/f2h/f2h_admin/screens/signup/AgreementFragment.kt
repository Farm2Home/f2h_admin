package com.f2h.f2h_admin.screens.signup

import android.app.Application
import android.os.Bundle
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.f2h.f2h_admin.R
import com.f2h.f2h_admin.databinding.FragmentAgreementBinding

class AgreementFragment: Fragment() {

    private lateinit var binding: FragmentAgreementBinding
    private val application: Application by lazy { requireNotNull(this.activity).application }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_agreement, container, false)
        binding.setLifecycleOwner(this)

        binding.accept.setOnClickListener {
            onAgreementAccepted()
        }

        binding.terms.text = "‘Farm to Home’ is a digital platform for selling farm products in India. The use of this platform is limited to persons or companies who are legally allowed to buy or sell farm products in accordance with the rules and regulations of the country.\n" +
                "This platform merely helps sellers to list farm products for sales and buyers to express interest in buying the products, with group administrators maintaining a record of payment transactions between the buyers and sellers. Products and home delivery services offered through the platform are owned by the sellers. ‘Farm to Home’ platform does not vouch for the quality and sustainability of products and delivery services offered by the sellers.\n" +
                "As a seller, you shall list only farm product(s) intended for selling on this platform. You must be legally able to sell the item(s) you list for sale on our platform and must have all the necessary licences and permits required for such sale in India and in the state in which you are operating. You shall also ensure full compliance with the tax regulations of the respective state as well as the government of India.\n" +
                "As a buyer, you are required to ascertain the credibility and quality of products and services offered by the sellers by directly contacting the seller yourself. ‘Farm to Home’ shall not take ownership for the products/services offered and it will not intervene in disputes, if any, between the buyer and the seller, including but not limited to, the quality of products and services or disputes related to payments. You shall not engage in or encourage the sale of any unlawful/contraband products through the platform. \n" +
                "Content posted on the platform by users, including the text, logos and pictures, or any other information (collectively ‘Content’) are third-party generated. ‘Farm to Home’ assumes no responsibility or liability over such content as we are merely an intermediary for supporting the sale of farm products. Any content posted by you, including photographs and descriptions, shall only be for the purpose of supporting the services rendered on this platform. ‘Farm to Home’ does not warrant that the product description or other content on the platform is accurate, complete, reliable, current, or error-free and assumes no liability in this regard. Posting of pornographic material, abusive language or anything potentially defamatory is strictly prohibited. ‘Farm to Home’ reserves the right to terminate the registration of any user on its platform and deny access to the platform if any unlawful or defamatory practices are observed.\n" +
                "Application administrators and administrators of all the groups that you join in the application will have access to personal details updated on the platform, including mobile numbers/contact information and user profile. Signing up in the application means that you have given consent to use of personal details given in the platform for services provided on the platform.\n" +
                "Registration on the platform is currently free as we are in the testing phase. ‘Farm to Home’ intends to become a paid service in the near future. Fee policy shall be clearly communicated to the users well in advance and their agreement shall be sought as and when the platform becomes a paid service.\n"

        return binding.root
    }

    private fun onAgreementAccepted() {
        Toast.makeText(this.context, "Accepted the terms and conditions", Toast.LENGTH_SHORT).show()
        val action = AgreementFragmentDirections.actionAgreementFragmentToSignUpFragment()
        view?.let { Navigation.findNavController(it).navigate(action) }
    }

}