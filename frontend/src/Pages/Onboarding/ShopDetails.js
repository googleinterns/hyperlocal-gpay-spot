import React from 'react';
import axios from 'axios';
import {Container, Form, Button} from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSpinner } from '@fortawesome/free-solid-svg-icons'
import ROUTES from '../../routes';
import LocationInput from '../../Components/LocationInput';

class ShopDetails extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
        pageLoading: false,
        shopID: null,
        shopName: '',
        typeOfService: '',
        addressLine1: '',
        latitude: 23.55555,
        longitude: 85.846925,
        showLocationInput: false
    };
  }

  hideLocationInput = () => {
    this.setState({showLocationInput: false});
    
  }

  setShopLocation = ({addressLine1, latitude, longitude}) => {
    this.setState({addressLine1, latitude, longitude});
  }

  submitDetails = () => {
    this.setState({pageLoading: true});
    axios.post("https://speedy-anthem-217710.an.r.appspot.com/api/insert/shop/", {
      merchantID: this.props.user.ID,
      shopName: this.state.shopName,
      typeOfService: this.state.typeOfService,
      addressLine1: this.state.addressLine1,
      latitude: this.state.latitude,
      longitude: this.state.longitude
    })
    .then(resp => {
      console.log(resp.data);
      if("shopID" in resp.data)
      {
        this.props.setShop({
          shopID: resp.data.shopID,
          shopName: this.state.shopName,
          typeOfService: this.state.typeOfService,
          addressLine1: this.state.addressLine1,
          latitude: this.state.latitude,
          longitude: this.state.longitude    
        });
        return this.props.history.push(ROUTES.merchant.onboarding.catalog);
      }
      else throw new Error(resp.data.error);
    })
    .catch(ex => {
      console.log(ex);
      this.setState({pageLoading: false});
    });
  }

  render() {
    return (
      <>
        <Container>
          <h3 className="h3 my-5">Seller Onboarding</h3>
          {
              this.state.pageLoading 
              ?   <div className="text-center mt-5"><FontAwesomeIcon icon={faSpinner} size="3x" /></div>
              :   <>
                      <Form.Group controlId="shopName" className="my-5">
                          <Form.Label>What should we call your shop?</Form.Label>
                          <Form.Control 
                              type="text" 
                              placeholder="Arvind Fruits Shop" 
                              value={this.state.shopName} 
                              onChange={e => this.setState({ shopName: e.target.value })}
                              autoComplete="off"
                              autoFocus
                          />
                      </Form.Group>

                      <Form.Group controlId="shopCategory" className="my-5">
                          <Form.Label>What do you plan to sell?</Form.Label>
                          <Form.Control value={this.state.typeOfService} onChange={e => this.setState({ typeOfService: e.target.value })} as="select">
                              <option disabled value=""> -- Select product/service -- </option>
                              <option value="food">Groceries</option>
                              <option value="garments">Garments</option>
                              <option value="electronics">Electronics Repair</option>
                              <option value="others">Others</option>
                          </Form.Control>
                      </Form.Group>
                      
                      <Form.Group controlId="shopLocation"  className="my-5">
                          <Form.Label>Where do you plan to sell?</Form.Label>
                          <Form.Control
                              type="text" 
                              placeholder="Type to search location" 
                              value={this.state.addressLine1} 
                              autoComplete="off"
                              style={{backgroundColor: '#fff'}}
                              readOnly
                              onClick={() => {this.setState({showLocationInput: true})}}
                          />
                      </Form.Group>
                      <LocationInput 
                        show={this.state.showLocationInput} 
                        onHide={this.hideLocationInput}
                        setLocation={this.setShopLocation} 
                        enforceSubmission={false}
                        />                      
                      <Button 
                          variant="primary" 
                          size="lg"
                          type="submit" 
                          block
                          className="fixedBottomBtn"
                          onClick={this.submitDetails}>
                          Proceed
                      </Button>
                  </>
          }
        </Container>
        </>
    );
  }
}

export default ShopDetails;