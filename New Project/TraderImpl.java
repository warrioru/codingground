

import java.util.concurrent.Semaphore;


class TraderImpl implements Trader {
    
        public Grain mine;
        public volatile Order stocked;
    
        public int WheatNeeded;
        public int BarleyNeeded;
        public int RiceNeeded;
        public int CornNeeded;
        
        Semaphore locks;
        

	

	public TraderImpl(Grain specialty) {
            this.WheatNeeded = 0;
            this.BarleyNeeded = 0;
            this.RiceNeeded = 0;
            this.CornNeeded = 0;
            
            mine = specialty;
            stocked = new Order();
            locks = new Semaphore(1);
	}

        @Override
	public synchronized Order getAmountOnHand() {
		
            return stocked;
	}

	
        @Override
	public void get(Order order) throws InterruptedException {
	    WheatNeeded = 0;
	    BarleyNeeded = 0;
	    RiceNeeded = 0;
	    CornNeeded = 0;
            
            
            while( true )
		{
			if ( Ratio( order ) ) // si esque ya tiene le da al brewer
			{
                            try{
                                locks.acquire();
                            
                                    for( Grain g : Grain.values() )
				{
					stocked.change(g, (-1 * (order.get(g))));
				}
				locks.release();
				break;
                                    
                        }
                        catch (InterruptedException ie) {}
                        }
			else
			{
				for( Grain g : Grain.values() )
				{
					if( g.equals(mine) )
					{
						if( order.get(g) > amount( mine ) ) { // si no tiene el suyo espera al supplier
                                                    delay();
                                                } 
					}
					else
					{
                                                checkTraders( g, order ); // si no tiene otros granos llama a los respectivos traders

                                            
					}
				}
			}
		}
		       
        }

	
        
        @Override
	public void swap(Grain what, int amt) throws InterruptedException {
            while(true){
                
            if(amt > stocked.get(mine)){
                    delay();
            } else {
                try{
                locks.acquire();
                stocked.change(what, amt);
                stocked.change(mine, (-1*amt));
                locks.release();
                break;
                }
                catch (InterruptedException ie) {}
            }
        }
		

	}
        
        private void checkTraders(Grain g, Order order) throws InterruptedException {
        
            if(stocked.get(g) > order.get(g)){
            } 
            else {
        
        P2.specialist(g).swap(mine, order.get(g));
        
        try{
        	locks.acquire();
                stocked.change(g, order.get(g));
                stocked.change(mine, (-1*order.get(g)));
                locks.release();
            }
            catch (InterruptedException ie) {}
            }     
      
    }
                
        @Override
	public synchronized void deliver(int amt) {
            stocked.change(mine, amt);	
            wakeUp();
	}

    private boolean Ratio(Order order) {
        BarleyNeeded = order.get(Grain.BARLEY);
        RiceNeeded = order.get(Grain.RICE);
        CornNeeded = order.get(Grain.CORN);
        WheatNeeded = order.get(Grain.WHEAT);
        
            return WheatNeeded < stocked.get(Grain.WHEAT) &&
                    RiceNeeded < stocked.get(Grain.RICE) &&
                    CornNeeded < stocked.get(Grain.CORN) &&
                    BarleyNeeded < stocked.get(Grain.BARLEY);
            
    }


    private int amount(Grain mine) {
        int actual = 0;
        
        if (Grain.RICE.equals(mine)){
            actual = stocked.get(Grain.RICE);
        }
        else if (Grain.BARLEY.equals(mine)){
            actual = stocked.get(Grain.BARLEY);
        }
        else if (Grain.CORN.equals(mine)){
            actual = stocked.get(Grain.CORN);
        }
        else if (Grain.WHEAT.equals(mine)){
            actual = stocked.get(Grain.WHEAT);
        }
        return actual;
    }
    
    private synchronized void wakeUp(){
        notifyAll();
    }

    private synchronized void delay() throws InterruptedException  {
                wait();

    }
    
}
