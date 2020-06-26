import React from 'react';
import axios from 'axios';

class HomePage extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      shopId: 2,
    };
  }

  fetchShopDetails = () => {
    axios.get('http://penguin.termina.linux.test/:8080/get/shop/'+this.state.shopId)
    .then(resp => {
      console.log(resp.data);
    })
    .catch(ex => {
      console.log(ex);
    })
    alert("Fetched");
    return;
  }

  render() {
    return (
      <>
        <input name="shopId" value={this.state.shopId} onChange={e => this.setState({ shopId: e.target.value })} autoComplete={"off"} />
        <button
          className="square"
          onClick={this.fetchShopDetails}
        >
          Get shop details
        </button>
      </>
    );
  }
}

export default HomePage;