import React from 'react';
import axios from 'axios';
import {Container} from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSpinner } from '@fortawesome/free-solid-svg-icons'
import ROUTES from '../../../routes';
import ShopDetailsInput from '../../../Components/ShopDetailsInput';

class ShopDetails extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
        pageLoading: false,
    };
  }

  componentDidUpdate(prevProps) {
    if(this.props.user.shop && !prevProps.user.shop) {
      this.props.history.push(ROUTES.merchant.onboarding.catalog);
    }
  }

  setShopDetails = (shop) => {
    this.setState({pageLoading: true});
    axios.post(ROUTES.v1.post.insertShop.replace(":merchantID", this.props.user.ID), {
      shopName: shop.shopName,
      typeOfService: shop.typeOfService,
      addressLine1: shop.addressLine1,
      latitude: shop.latitude,
      longitude: shop.longitude
    })
    .then(resp => {
      if("shopID" in resp.data) this.props.setShop(resp.data);
      else throw new Error(resp.data.error);
    })
    .catch(ex => {
      console.log(ex);
      this.setState({pageLoading: false});
    });
  }

  render() {
    return (
        <Container>
          <h3 className="h3 my-5">Seller Onboarding</h3>
          {
              this.state.pageLoading 
              ?   <div className="text-center mt-5"><FontAwesomeIcon icon={faSpinner} size="3x" /></div>
              :   <ShopDetailsInput submit="Proceed" setShopDetails={this.setShopDetails} />
          }
        </Container>
    );
  }
}

export default ShopDetails;
