import React from 'react';
import { Button, Form, Modal } from 'react-bootstrap';

class LocationInput extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      addressLine1: '',
    };
    this.addressLine1Input = React.createRef();
    this.autocompleteObj = null;
    this.geocoderObj = null;
  }

  componentDidMount() {
    if(this.props.show) {
      this.initGeocoderAndAutocomplete();
    }
  }

  componentDidUpdate(prevProps) {
    if(this.props.show && !prevProps.show) {
      this.initGeocoderAndAutocomplete();
    }
  }

  initGeocoderAndAutocomplete = () => {
    this.geocoder = new window.google.maps.Geocoder;
    this.autocompleteObj = new window.google.maps.places.Autocomplete(this.addressLine1Input.current, {'fields': ['name', 'formatted_address','geometry']});
    this.autocompleteObj.addListener('place_changed', this.handleAddressLine1Update);
    this.addressLine1Input.current.focus();
  }

  onHide = () => {
    this.setState({addressLine1: ''});
    this.props.onHide();
  }

  setLocation = location => {
    this.props.setLocation(location);
    this.onHide();
  }

  handleAddressLine1Update = () => {
    const selectedPlace = this.autocompleteObj.getPlace();
    
    let selectedPlaceAddressLine1 = selectedPlace.name+", "+selectedPlace.formatted_address;
    // Removes redundant parts of address
    if(selectedPlace.name === selectedPlace.formatted_address.split(",")[0]) 
      selectedPlaceAddressLine1 = selectedPlace.formatted_address;
    
    let selectedPlaceCoordinates = selectedPlace.geometry.location;
    // Use center of geo bounds if needed
    if(selectedPlaceCoordinates === undefined) selectedPlace.geometry.viewport.getCenter();
    
    this.setLocation({
      'addressLine1': selectedPlaceAddressLine1,
      'latitude': selectedPlaceCoordinates.lat(),
      'longitude': selectedPlaceCoordinates.lng()
    });
  }
  
  useCurrentLocation = () => {
    window.microapps.getCurrentLocation()
    .then(location => {
      this.geocoder.geocode({
        'location': {
          lat: location.latitude, 
          lng: location.longitude
        }}, (results, status) => {
        if (status !== 'OK')
        {
          window.alert(status);
          return;
        }
        if(!results[0]) results = [{formatted_address: 'Current Location'}];
        this.setLocation({
          'addressLine1': results[0].formatted_address,
          'latitude': location.latitude,
          'longitude': location.longitude
        });
      });
    });
  }

  render() {
    return (
        <Modal
            centered
            show={this.props.show}
            onHide={this.onHide}
            backdrop={this.props.enforceSubmission ? 'static' : true}
            >
            <Modal.Header closeButton={this.props.enforceSubmission ? false : true }>
            <Modal.Title>Location Needed</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Button variant="primary" onClick={this.useCurrentLocation} block> Use Current Location</Button>
                <hr />
                <Form.Group controlId="addServiceName">
                <Form.Control 
                  type="text" 
                  ref={this.addressLine1Input}
                  placeholder="Enter an address/locality"  
                  autoComplete="off" 
                  value={this.state.addressLine1}
                  onChange={e => this.setState({ addressLine1: e.target.value })}
                   />
                </Form.Group>

            </Modal.Body>
        </Modal>
    );
  }
}

LocationInput.defaultProps = {
  enforceSubmission: true
};

export default LocationInput;