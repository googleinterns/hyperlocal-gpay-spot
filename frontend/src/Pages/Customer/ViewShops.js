import axios from 'axios';
import React from 'react';
import { Col, Button, Card, Container, Form, ListGroup } from 'react-bootstrap';
import { Link } from "react-router-dom";
import '../../App.css';
import LocationInput from '../../Components/LocationInput';
import ROUTES from '../../routes';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSpinner } from '@fortawesome/free-solid-svg-icons';
import Autocomplete from '@material-ui/lab/Autocomplete';
import TextField from '@material-ui/core/TextField';
import parse from 'autosuggest-highlight/parse';
import match from 'autosuggest-highlight/match';


class ViewShops extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      shops: [],
      pageLoading: true,
      searchQuery: "",
      queryRadius: "",
      showModal: true,
      searchSuggestions: []
    }
  }

  getSearchConfig = () => {
    let requestParams = {};
    requestParams.latitude = this.props.latitude;
    requestParams.longitude = this.props.longitude;

    if (this.state.searchQuery !== "") {
      requestParams.query = this.state.searchQuery;
    }

    if (this.state.queryRadius !== "") {
      requestParams.queryRadius = this.state.queryRadius;
    }

    const config = {
      method: 'get',
      url: ROUTES.v1.get.index.search,
      headers: {},
      params: requestParams
    };

    return config;
  }

  search = async () => {
    this.setState({ pageLoading: true });
    const configParams = this.getSearchConfig();
    const shopDetailsSnippets = (await axios(configParams)).data;
    let shopDetailsList = [];

    shopDetailsSnippets.map((shopDetailsSnippet) => {
      shopDetailsList.push(shopDetailsSnippet.shopDetails);
    });

    this.setState({
      "shops": shopDetailsList,
      searchQuery: "",
      queryRadius: "",
      pageLoading: false,
      searchSuggestions: []
    })
  }

  updateSuggestions = async () => {
    const configParams = this.getSearchConfig();
    const shopDetailsSnippets = (await axios(configParams)).data;
    let searchSuggestions = [];
    shopDetailsSnippets.map(shopSnippet => {
      if (Array.isArray(shopSnippet.matchedPhrases) && shopSnippet.matchedPhrases.length) {
        // Search Index wraps the matched words in <em> tags by default, removing the tags here
        let suggestion = shopSnippet.matchedPhrases[0].replace('<em>', '').replace('</em>', '');
        searchSuggestions.push(suggestion);
      }
    });

    // To ensure that duplicate suggestions aren't there
    searchSuggestions = [...new Set(searchSuggestions)]
    this.setState({ searchSuggestions });
  }

  searchBoxUpdateHandler = (e, updatedSearchBoxValue) => {
    this.setState({ searchQuery: updatedSearchBoxValue });
    this.updateSuggestions();
  }

  onHide = () => {
    this.setState({
      showModal: false
    });
  }

  convertDegreeToRadians = (valueInDegree) => {
    return Math.PI * valueInDegree / 180.0
  }

  // Calculate distance to a Shop from user's current coordinates using Haversine formula: https://en.wikipedia.org/wiki/Haversine_formula
  getDistanceToShop = (shop) => {
    const EARTH_RADIUS_IN_KM = 6371;

    // latitude and longitude of both points (User's current Coordinates and Shop's coordinates in radian)
    const firstPointLatitudeInRadians = this.convertDegreeToRadians(shop["shop"]["latitude"]);
    const firstPointLongitudeInRadians = this.convertDegreeToRadians(shop["shop"]["longitude"]);
    const secondPointLatitudeInRadians = this.convertDegreeToRadians(this.props.latitude);
    const secondPointLongitudeInRadians = this.convertDegreeToRadians(this.props.longitude);

    const deltaLatitudeInRadians = Math.abs(secondPointLatitudeInRadians - firstPointLatitudeInRadians);
    const deltaLongitudeInRadians = Math.abs(secondPointLongitudeInRadians - firstPointLongitudeInRadians);

    // Haversine formula
    const angleBetweenCoordinatesInRadians = 2 * Math.asin(
      Math.sqrt(Math.sin(deltaLatitudeInRadians / 2) * Math.sin(deltaLatitudeInRadians / 2)
        + Math.cos(firstPointLatitudeInRadians) * Math.cos(secondPointLatitudeInRadians)
        * Math.sin(deltaLongitudeInRadians / 2) * Math.sin(deltaLongitudeInRadians / 2)
      )
    );

    // distance = radius * angle subtended
    return (angleBetweenCoordinatesInRadians * EARTH_RADIUS_IN_KM).toFixed(2);
  }

  changeLocation = () => {
    this.props.setLocation({
      latitude: null,
      longitude: null
    });
    this.setState({
      showModal: true
    });
  }

  componentDidUpdate(prevProps) {
    if (this.props.latitude != null && (this.props.latitude !== prevProps.latitude || this.props.longitude !== prevProps.longitude)) {
      this.search();
    }
  }

  componentDidMount() {
    if (this.props.latitude !== null || (this.props.longitude !== null)) {
      this.search();
    }
  }

  render() {
    if (this.props.latitude == null || this.props.longitude == null) {
      return (
        <LocationInput setLocation={this.props.setLocation} show={this.state.showModal} onHide={this.onHide} />
      );
    } else {
      return (
        this.state.pageLoading
          ? <div className="text-center mt-5"><FontAwesomeIcon icon={faSpinner} size="3x" /></div>
          : <Container className="mt-1 p-3">
            <Button variant="primary" className="mb-2" onClick={this.changeLocation} block> Change Location</Button>
            <Form onSubmit={(e) => { e.preventDefault(); this.search(); }}>
              <Form.Row className="align-items-center">
                <Col xs={7}>
                  <Autocomplete
                    freeSolo
                    id="autocomplete"
                    options={this.state.searchSuggestions}
                    getOptionLabel={(suggestion) => suggestion}
                    renderInput={(params) => <TextField {...params} label="Search Nearby" variant="outlined" size="small" />}
                    onInputChange={this.searchBoxUpdateHandler}
                    onChange={this.search}
                    renderOption={(suggestion, { inputValue }) => {
                      const matches = match(suggestion, inputValue);
                      const parts = parse(suggestion, matches);
                      return (
                        <div>
                          {parts.map((part, index) => (
                            <span key={index} style={{ fontWeight: part.highlight ? 700 : 400 }}>
                              {part.text}
                            </span>
                          ))}
                        </div>
                      );
                    }}
                  />
                </Col>
                <Col xs={3}>
                  <Form.Control
                    placeholder="3km"
                    aria-label="distance"
                    onChange={e => this.setState({ queryRadius: e.target.value })}
                  />
                </Col>
                <Col xs={2}>
                  <Button variant="success" onClick={this.search}>
                    Go
                </Button>
                </Col>
              </Form.Row>
            </Form>

            <ListGroup variant="flush" className="mt-4">
              {
                this.state.shops.map(shop => {
                  const shopDistanceInKM = this.getDistanceToShop(shop);
                  return (
                    <ListGroup.Item key={shop["shop"]["shopID"]}>
                      <Card className="mb-4" style={{ backgroundColor: "#E3F2FD" }}>
                        <Card.Body>
                          <Card.Title>{shop["shop"]["shopName"]}</Card.Title>
                          <Card.Text>
                            <i><b>{shopDistanceInKM} km away</b></i>
                            <br />
                            <b>Sold By: </b>{shop["merchant"]["merchantName"]}
                            <br />
                            <b>At: </b>{shop["shop"]["addressLine1"]}
                            <br />
                            <b>Reach out at: </b>{shop["merchant"]["merchantPhone"]}
                          </Card.Text>
                          <Button variant="info">
                            <Link
                              to={{
                                pathname: ROUTES.customer.catalog.replace(':shopid', shop["shop"]["shopID"]),
                              }} className="btn btn-info box">
                              View Catalog
                          </Link>
                          </Button>
                        </Card.Body>
                      </Card>
                    </ListGroup.Item>
                  )
                })
              }
            </ListGroup>
          </Container>
      );
    }
  }
}

export default ViewShops;
