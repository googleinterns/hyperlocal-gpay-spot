import React from 'react';
import axios from 'axios';
import ROUTES from '../../routes';
import {Container, Button, Card, Row, Col, Modal, Form } from 'react-bootstrap';
import { Container as FABContainer, Button as FAButton } from 'react-floating-action-button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus, faTimes, faSpinner } from '@fortawesome/free-solid-svg-icons'

class Catalog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
        pageLoading: true,
        showAddForm: false,
        addForm: {
          'serviceName': '',
          'serviceDescription': '',
          'serviceImageURL': '',
        },
        showEditForm: false,
        editForm: {
            'serviceName': '',
            'serviceDescription': '',
            'serviceImageURL': '',  
        },
        catalog : [
        ],
        initialCatalog: []
    };
  }

  componentDidMount() {
    return this.resetAndReloadServices();
  }

  submitCatalog = () => {
    this.setState({pageLoading: true});
    let initialCatalog = this.state.initialCatalog;
    let currentCatalog = this.state.catalog;

    let itemsToDelete = [];
    for(let i = 0; i < initialCatalog.length; i++)
    {
        let curItemKey = initialCatalog[i].key;
        let curItemKeyExists = false;
        for(let j = 0; j < currentCatalog.length; j++)
        {
            if(currentCatalog[j].key === curItemKey)
            {
                curItemKeyExists = true;
                break;
            }
        }
        if(!curItemKeyExists)
            itemsToDelete.push({serviceID: initialCatalog[i].serviceID});
    }

    let itemsToAdd = [];
    for(let i = 0; i < currentCatalog.length; i++)
    {
        if(!("serviceID" in currentCatalog[i]))
        {
            let itemToAdd = currentCatalog[i];
            itemsToAdd.push({
                serviceName: itemToAdd.serviceName,
                serviceDescription: itemToAdd.serviceDescription,
                imageURL: itemToAdd.serviceImageURL
            });
            
        }
    }

    let itemsToEdit = [];
    for(let i = 0; i < initialCatalog.length; i++)
    {
        let curItemKey = initialCatalog[i].key;
        for(let j = 0; j < currentCatalog.length; j++)
        {
            if(currentCatalog[j].key !== curItemKey) continue;
            let itemBefore = initialCatalog[i];
            let itemAfter = currentCatalog[j];
            if( itemBefore.serviceName !== itemAfter.serviceName ||
                itemBefore.serviceDescription !== itemAfter.serviceDescription ||
                itemBefore.serviceImageURL !== itemAfter.serviceImageURL)
            {
                itemsToEdit.push({
                    serviceID: itemAfter.serviceID,
                    serviceName: itemAfter.serviceName,
                    serviceDescription: itemAfter.serviceDescription,
                    imageURL: itemAfter.serviceImageURL        
                });
            }
            break;
        }
    }


    console.log("Add: ", itemsToAdd);
    console.log("Edit: ", itemsToEdit);
    console.log("Delete: ", itemsToDelete);

    if(!itemsToAdd.length && !itemsToEdit.length && !itemsToDelete.length) return this.props.history.push(ROUTES.merchant.dashboard);

    axios.post("https://speedy-anthem-217710.an.r.appspot.com/api/shop/"+this.state.shopID+"/catalog/update", {
      add: itemsToAdd,
      edit: itemsToEdit,
      delete: itemsToDelete
    })
    .then(resp => {
      if(resp.data.success) return this.props.history.push(ROUTES.merchant.dashboard);
      else alert(resp.data.error);
    })
    .catch(ex => {
      console.log(ex);
    });
    
  }

  resetAndReloadServices = () => {
    return axios.get("https://speedy-anthem-217710.an.r.appspot.com/api/shop/"+this.props.user.shop.shopID)
    .then(res => {
      if(!("catalog" in res.data))
          throw new Error(res.data.error);
      let catalog = res.data.catalog;
        catalog.forEach((item, index) => {
          item.key = index.toString();
          item.serviceImageURL = item.imageURL;
        });
        this.setState({initialCatalog: catalog, catalog, pageLoading: false});
        return;
    })
    .catch(err => {
        console.log(err);
        alert("Whoops, something went wrong. Trying again...");
        return this.resetAndReloadServices();
    });
  }


  addProduct = () => {
    
    /* To-do: Validate input */

    let itemsList = this.state.catalog;
    let newItem = {
      serviceName: this.state.addForm.serviceName,
      serviceDescription: this.state.addForm.serviceDescription,
      serviceImageURL: this.state.addForm.serviceImageURL,
      key: Date.now().toString()
    };
    itemsList.push(newItem);
    this.setState({catalog: itemsList});
    this.hideAddForm();
    return;
  }

  deleteProduct = (productKey) => {
    let existingItemsList = this.state.catalog;
    let newItemsList = existingItemsList.filter(item => item.key !== productKey);
    this.setState({catalog: newItemsList});
    return;
  }

  addNewServiceImageURL = (e) => {
    this.setState({
      addForm: {
        ...this.state.addForm,
        serviceImageURL: e.target.value
      }
    });
    return;
  }

  addNewServiceDescription = (e) => {
    this.setState({
      addForm: {
        ...this.state.addForm,
        serviceDescription: e.target.value 
      }
    });
    return;
  }

  addNewServiceName = (e) => {
    this.setState({
      addForm: {
        ...this.state.addForm,
        serviceName: e.target.value 
      }
    });
    return;
  }

  showAddForm = () => {
    this.setState({showAddForm: true});
  }

  hideAddForm = () => {
    this.setState({showAddForm: false, addForm: {serviceName: '', serviceDescription: '', serviceImageURL: ''}});
  }

  
  render() {
    let products = this.state.catalog.map((product) => {
        return (
            <React.Fragment key={product.key}>
            <Row>
                <Col xs={4}>
                    <Card.Img variant="top" src={product.serviceImageURL} />
                </Col>
                <Col xs={8}>
                    <h6>
                      {product.serviceName}
                      <button
                        className="pull-right"
                        onClick={e => {e.preventDefault(); this.deleteProduct(product.key)}}
                        style={{
                          marginRight: '10px',
                          color: 'white',
                          backgroundColor: '#d64941',
                          padding: '0px 4px',
                          borderRadius: '3px',
                          border: '0px',
                        }}
                      >
                        <FontAwesomeIcon icon={faTimes} size="sm" />
                      </button>
                    </h6>
                    <p className="small" style={{marginBottom: '0px'}}>{product.serviceDescription}</p>
                </Col>
            </Row>
            <hr />
            </React.Fragment>
        );
    });

    return (
      <>
      <Container className="mb-5">
        <h3 className="h3 mt-5 mb-4">My Catalog</h3>
        <p>Update or add new services</p>
        <hr />
        {
            this.state.pageLoading
            ? <div className="text-center mt-5"><FontAwesomeIcon icon={faSpinner} size="3x" /></div>
            : this.state.catalog.length === 0
            ? <p className="text-center my-5">There's nothing here. Add something by clicking the Plus button below!</p>
            : <>{products}</>
        }

      </Container>

      <Modal
        show={this.state.showAddForm}
        onHide={this.hideAddForm}
      >
        <Modal.Header closeButton>
          <Modal.Title>Add Product/Service</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={e => {e.preventDefault(); return this.addProduct();}}>
            <Form.Group controlId="addServiceName">
              <Form.Label>What are you selling?</Form.Label>
              <Form.Control type="text" placeholder="Dasheri Mangos" onChange={this.addNewServiceName} autoComplete="off" />
            </Form.Group>

            <Form.Group controlId="addServiceDescription">
              <Form.Label>Brief description</Form.Label>
              <Form.Control as="textarea" rows="2" placeholder="Dasheri mangos fresh off the market!" onChange={this.addNewServiceDescription} />
            </Form.Group>

            <Form.Group controlId="addServiceImageURL">
              <Form.Label>Image URL</Form.Label>
              <Form.Control type="text" placeholder="https://example.com/mangos.jpg" onChange={this.addNewServiceImageURL} autoComplete="off" />
            </Form.Group>
          </Form>

        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={this.hideAddForm}>
            Cancel
          </Button>
          <Button variant="primary" onClick={this.addProduct}>Add</Button>
        </Modal.Footer>
      </Modal>
      {
          !this.state.pageLoading && 
          <FABContainer>
            <FAButton
                styles={{
                    backgroundColor: '#343a40', 
                    color: '#fff',
                    marginBottom: '5px',
                    marginRight: '-2px',
                }}
                tooltip="Add products!"
                onClick={this.showAddForm}>
                    <FontAwesomeIcon icon={faPlus} size="lg" />
            </FAButton>
          </FABContainer>
      }
      <Button 
        onClick={this.submitCatalog}
        variant="primary" 
        size="lg"
        block
        disabled={this.state.pageLoading}
        className="fixedBottomBtn">
        Update
      </Button>
      </>
    );
  }
}

export default Catalog;