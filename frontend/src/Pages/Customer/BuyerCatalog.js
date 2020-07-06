import React from 'react';
import axios from 'axios';
import { Row, Col, Button, Container, Jumbotron, Card, Image, ListGroup } from 'react-bootstrap';
import ROUTES from '../../routes';

const CALL_BUTTON_IMAGE_URL = "/images/call.png";

class BuyerCatalog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      shopID: props.match.params.shopid,
      shopFetched: false
    };
  }

  componentDidMount() {
    this.getShopDetails();
  }

  getShopDetails() {
    const shopID = this.state.shopID;
    axios.get(ROUTES.api.get.shopByShopID.replace("%b", shopID))
      .then((response) => {
        response.data["shopFetched"] = true;
        this.setState(response.data);
      });
  }

  render() {
    if (this.state.shopFetched === false) {
      return (
        <Container className="p-0 ">
          <Jumbotron>
          </Jumbotron>
        </Container>
      );
    } else {
      return (
        <>
          <Container className="p-0 ">
            <Jumbotron>
              <Row className="p-0">
                <Col>
                  <h4>{this.state.shop.shopName}</h4>
                </Col>
                <Col>
                  <Button className="icon">
                    <a href={"tel:" + this.state.merchant.merchantPhone}>
                      <Image src={CALL_BUTTON_IMAGE_URL} className="photo" />
                    </a>
                  </Button>
                </Col>
              </Row>
            </Jumbotron>
          </Container>
          <Container>
            <ListGroup variant="flush" className="mt-4">
              {
                this.state.catalog.map(
                  function (catalogItem) {
                    return (
                      <Row className="p-2" key={catalogItem.serviceID}>
                        <Col>
                          <ListGroup.Item key={catalogItem.serviceID}>
                            <Image src={catalogItem.imageURL} thumbnail />
                            <Card style={{ backgroundColor: "#E3F2FD" }}>
                              <Card.Body>
                                <Card.Title>{catalogItem.serviceName}</Card.Title>
                                <Card.Text>
                                  {catalogItem.serviceDescription}
                                </Card.Text>
                              </Card.Body>
                            </Card>
                          </ListGroup.Item>
                        </Col>
                      </Row>
                    );
                  }
                )
              }
            </ListGroup>
          </Container>
        </>
      );
    }
  }
}

export default BuyerCatalog;