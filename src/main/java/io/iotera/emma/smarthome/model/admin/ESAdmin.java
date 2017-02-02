package io.iotera.emma.smarthome.model.admin;

import javax.persistence.*;

@Entity
@Table(name = "admin_tbl")
public class ESAdmin {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    protected long id;

    @Column(nullable = false)
    protected String token;

}
