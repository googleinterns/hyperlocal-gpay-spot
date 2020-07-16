import React from 'react';
import axios from 'axios';
import ROUTES from '../../../routes';
import { Container } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faSpinner } from '@fortawesome/free-solid-svg-icons'
import CatalogInput from '../../../Components/CatalogInput';

class Catalog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
        pageLoading: true,
        initialCatalog: []
    };
  }

  componentDidMount() {
    return this.resetAndReloadServices();
  }

  resetAndReloadServices = () => {
    return axios.get(ROUTES.v1.get.shopByShopID.replace("%b", this.props.user.shop.shopID))
    .then(res => {
      if(!("catalog" in res.data))
          throw new Error(res.data.error);
      let initialCatalog = res.data.catalog;
      initialCatalog.forEach((item, index) => {
        item.key = index.toString();
        item.serviceImageURL = item.imageURL;
      });
      this.setState({ initialCatalog, pageLoading: false });
      return;
    })
    .catch(err => {
        console.log(err);
        alert("Whoops, something went wrong. Trying again...");
        return this.resetAndReloadServices();
    });
  }

  setCatalog = ({ itemsToCreate, itemsToUpdate, itemsToDelete }) => {
    this.setState({pageLoading: true});

    if(!itemsToCreate.length && !itemsToUpdate.length && !itemsToDelete.length) return this.props.history.push(ROUTES.merchant.dashboard);

    axios.post(ROUTES.v1.post.updateCatalog.replace(":shopID", this.props.user.shop.shopID), {
      create: itemsToCreate,
      update: itemsToUpdate,
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

  render() {
    return (
      <Container className="mb-5">
        <h3 className="h3 mt-5 mb-4">My Catalog</h3>
        <p>Update or add new services</p>
        <hr />
        {
            this.state.pageLoading
            ? <div className="text-center mt-5"><FontAwesomeIcon icon={faSpinner} size="3x" /></div>
            : <CatalogInput value={this.state.initialCatalog} setCatalog={this.setCatalog} />
        }
      </Container>
    );
  }

}

export default Catalog;